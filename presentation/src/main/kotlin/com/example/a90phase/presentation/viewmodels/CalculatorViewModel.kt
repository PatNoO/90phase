package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.CalculateOptimalBedtimeUseCase
import com.example.a90phase.domain.usecases.FetchSystemAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmRepository: AlarmRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val calculateOptimalBedtimeUseCase = CalculateOptimalBedtimeUseCase(userPreferencesRepository)
    private val fetchSystemAlarmsUseCase = FetchSystemAlarmsUseCase(alarmRepository)

    private val _uiState = MutableStateFlow<SleepCalculatorUiState>(SleepCalculatorUiState.Loading)
    val uiState: StateFlow<SleepCalculatorUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    private var lastWakeTime: LocalTime = LocalTime.of(7, 0)

    init {
        // Load the wake time the user last chose so it survives app restarts.
        viewModelScope.launch {
            val savedWakeTime = userPreferencesRepository.observeSelectedWakeTime().first()
            lastWakeTime = savedWakeTime
            recalculate(savedWakeTime)
        }
    }

    fun onWakeTimeChanged(wakeTime: LocalTime) {
        val previousWakeTime = lastWakeTime
        lastWakeTime = wakeTime
        viewModelScope.launch {
            // Persist so the wake time is remembered across app restarts.
            userPreferencesRepository.setSelectedWakeTime(wakeTime.hour, wakeTime.minute)
            if (previousWakeTime != wakeTime) {
                // Keep an active alarm in sync with the new wake time (setAlarmClock replaces it).
                if (userPreferencesRepository.observeWakeAlarmEnabled().first()) {
                    notificationScheduler.scheduleWakeAlarm(wakeTime)
                }
                // The morning rating fires at wake time + 15 min, so it has to move too. Without
                // this it kept firing against the previous wake time until the next time it fired
                // and re-armed itself — a full day late, or hours early.
                if (userPreferencesRepository.observeMorningRatingEnabled().first()) {
                    notificationScheduler.scheduleMorningFeedback(wakeTime)
                }
            }
            recalculate(wakeTime)
        }
    }

    fun retry() {
        viewModelScope.launch { recalculate(lastWakeTime) }
    }

    private suspend fun recalculate(wakeTime: LocalTime) {
        _uiState.value = SleepCalculatorUiState.Loading
        val nextAlarm = fetchSystemAlarmsUseCase().getOrNull()?.firstOrNull()
        val dailyCheckInEnabled = userPreferencesRepository.observeDailyCheckInEnabled().first()
        val alarmActive = userPreferencesRepository.observeWakeAlarmEnabled().first()
        when (val result = calculateOptimalBedtimeUseCase(wakeTime)) {
            is Result.Success -> _uiState.value = SleepCalculatorUiState.Success(
                wakeTime = wakeTime,
                bedtimes = result.data,
                nextSystemAlarm = nextAlarm,
                dailyCheckInEnabled = dailyCheckInEnabled,
                alarmActive = alarmActive,
            )
            is Result.Error -> _uiState.value = SleepCalculatorUiState.Error(
                messageRes = result.error.toMessageRes(),
            )
            is Result.Loading -> Unit
        }
    }

    fun onBedtimeSelected(recommendation: BedtimeRecommendation, index: Int) {
        val current = _uiState.value as? SleepCalculatorUiState.Success ?: return
        val newIndex = if (current.selectedBedtimeIndex == index) -1 else index
        _uiState.value = current.copy(selectedBedtimeIndex = newIndex)
        viewModelScope.launch {
            if (newIndex == -1) {
                // Deselecting clears any wind-down reminder scheduled for the old bedtime.
                notificationScheduler.cancelBedtimeReminder()
                return@launch
            }
            userPreferencesRepository.setSelectedBedtime(
                hour = recommendation.bedtime.hour,
                minute = recommendation.bedtime.minute,
                cycleCount = recommendation.cycleCount,
                durationMinutes = recommendation.durationMinutes,
            )
            if (userPreferencesRepository.observeBedtimeReminderEnabled().first()) {
                notificationScheduler.scheduleBedtimeReminder(recommendation.bedtime)
            }
        }
    }

    fun onAlarmActiveToggled(enabled: Boolean) {
        val current = _uiState.value as? SleepCalculatorUiState.Success ?: return
        _uiState.value = current.copy(alarmActive = enabled)
        viewModelScope.launch {
            userPreferencesRepository.setWakeAlarmEnabled(enabled)
            if (enabled) {
                notificationScheduler.scheduleWakeAlarm(current.wakeTime)
            } else {
                notificationScheduler.cancelWakeAlarm()
            }
            // Refresh the "Din alarm" banner so it reflects the new alarm state.
            val nextAlarm = fetchSystemAlarmsUseCase().getOrNull()?.firstOrNull()
            val refreshed = _uiState.value as? SleepCalculatorUiState.Success ?: return@launch
            _uiState.value = refreshed.copy(nextSystemAlarm = nextAlarm)
        }
    }

    fun onDailyCheckInToggled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyCheckInEnabled(enabled)
            if (enabled) {
                val reminderTime = userPreferencesRepository.observeUserProfile().first().reminderTime
                notificationScheduler.scheduleDailyCheckIn(reminderTime)
            } else {
                notificationScheduler.cancelDailyCheckIn()
            }
            val refreshed = _uiState.value as? SleepCalculatorUiState.Success ?: return@launch
            _uiState.value = refreshed.copy(dailyCheckInEnabled = enabled)
        }
    }

    fun onSaveClicked() {
        val current = _uiState.value as? SleepCalculatorUiState.Success ?: return
        if (current.selectedBedtimeIndex < 0) return
        val recommendation = current.bedtimes[current.selectedBedtimeIndex]
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            // Save tonight's plan — persist the chosen bedtime and schedule its wind-down
            // reminder. This does NOT create a sleep-history entry; the actual log is recorded
            // the next morning via the rating flow.
            val result = userPreferencesRepository.setSelectedBedtime(
                hour = recommendation.bedtime.hour,
                minute = recommendation.bedtime.minute,
                cycleCount = recommendation.cycleCount,
                durationMinutes = recommendation.durationMinutes,
            )
            if (userPreferencesRepository.observeBedtimeReminderEnabled().first()) {
                notificationScheduler.scheduleBedtimeReminder(recommendation.bedtime)
            }
            val refreshed = _uiState.value as? SleepCalculatorUiState.Success ?: return@launch
            _uiState.value = refreshed.copy(isSaving = false)
            _saveResult.emit(result is Result.Success)
        }
    }
}
