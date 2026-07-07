package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.CalculateOptimalBedtimeUseCase
import com.example.a90phase.domain.usecases.FetchSystemAlarmsUseCase
import com.example.a90phase.domain.usecases.LogSleepSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
    sleepRepository: SleepRepository,
) : ViewModel() {

    private val calculateOptimalBedtimeUseCase = CalculateOptimalBedtimeUseCase(userPreferencesRepository)
    private val fetchSystemAlarmsUseCase = FetchSystemAlarmsUseCase(alarmRepository)
    private val logSleepSessionUseCase = LogSleepSessionUseCase(sleepRepository)

    private val _uiState = MutableStateFlow<SleepCalculatorUiState>(SleepCalculatorUiState.Loading)
    val uiState: StateFlow<SleepCalculatorUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    private var lastWakeTime: LocalTime = LocalTime.of(7, 0)

    init {
        onWakeTimeChanged(lastWakeTime)
    }

    fun onWakeTimeChanged(wakeTime: LocalTime) {
        val previousWakeTime = lastWakeTime
        val wasAlarmActive = (_uiState.value as? SleepCalculatorUiState.Success)?.alarmActive ?: false
        lastWakeTime = wakeTime
        viewModelScope.launch {
            _uiState.value = SleepCalculatorUiState.Loading
            // Keep an active alarm in sync: replace the old one with a new one at the new time.
            if (wasAlarmActive && previousWakeTime != wakeTime) {
                alarmRepository.dismissAlarm()
                alarmRepository.setAlarm(wakeTime)
            }
            val nextAlarm = fetchSystemAlarmsUseCase().getOrNull()?.firstOrNull()
            val dailyCheckInEnabled = userPreferencesRepository.observeDailyCheckInEnabled().first()
            when (val result = calculateOptimalBedtimeUseCase(wakeTime)) {
                is Result.Success -> _uiState.value = SleepCalculatorUiState.Success(
                    wakeTime = wakeTime,
                    bedtimes = result.data,
                    nextSystemAlarm = nextAlarm,
                    dailyCheckInEnabled = dailyCheckInEnabled,
                    alarmActive = wasAlarmActive,
                )
                is Result.Error -> _uiState.value = SleepCalculatorUiState.Error(
                    message = result.error.toSwedishMessage(),
                )
                is Result.Loading -> Unit
            }
        }
    }

    fun retry() = onWakeTimeChanged(lastWakeTime)

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
            if (enabled) {
                alarmRepository.setAlarm(current.wakeTime)
            } else {
                alarmRepository.dismissAlarm()
            }
            // Refresh the "Din alarm" banner so it reflects the new system-alarm state.
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
            val profile = userPreferencesRepository.observeUserProfile().first()
            val today = LocalDate.now()
            val wakeInstant = today.atTime(current.wakeTime).atZone(ZoneId.systemDefault()).toInstant()
            // Derive bedtime from the wake instant minus the recommended sleep length so the
            // pair is always exactly durationMinutes apart and lands on the correct calendar
            // day, even when the bedtime crosses midnight (e.g. a 2-cycle 03:45 bedtime).
            val bedtimeInstant = wakeInstant.minus(Duration.ofMinutes(recommendation.durationMinutes.toLong()))
            val result = logSleepSessionUseCase.createLog(
                date = today,
                wakeTime = wakeInstant,
                bedtime = bedtimeInstant,
                cycleCount = recommendation.cycleCount,
                cycleDuration = profile.optimalCycleMinutes,
                sleepLatency = profile.sleepLatencyMinutes,
            )
            val refreshed = _uiState.value as? SleepCalculatorUiState.Success ?: return@launch
            _uiState.value = refreshed.copy(isSaving = false)
            _saveResult.emit(result is Result.Success)
        }
    }
}
