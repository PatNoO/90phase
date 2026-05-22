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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    alarmRepository: AlarmRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val calculateOptimalBedtimeUseCase = CalculateOptimalBedtimeUseCase(userPreferencesRepository)
    private val fetchSystemAlarmsUseCase = FetchSystemAlarmsUseCase(alarmRepository)

    private val _uiState = MutableStateFlow<SleepCalculatorUiState>(SleepCalculatorUiState.Loading)
    val uiState: StateFlow<SleepCalculatorUiState> = _uiState.asStateFlow()

    private var lastWakeTime: LocalTime = LocalTime.of(7, 0)

    init {
        onWakeTimeChanged(lastWakeTime)
    }

    fun onWakeTimeChanged(wakeTime: LocalTime) {
        lastWakeTime = wakeTime
        viewModelScope.launch {
            _uiState.value = SleepCalculatorUiState.Loading
            val nextAlarm = fetchSystemAlarmsUseCase().getOrNull()?.firstOrNull()
            when (val result = calculateOptimalBedtimeUseCase(wakeTime)) {
                is Result.Success -> _uiState.value = SleepCalculatorUiState.Success(
                    wakeTime = wakeTime,
                    bedtimes = result.data,
                    nextSystemAlarm = nextAlarm,
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
        _uiState.value = current.copy(selectedBedtimeIndex = index)
        viewModelScope.launch {
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
}
