package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.StartDiscoveryPhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    sleepRepository: SleepRepository,
    private val patternInsightsRepository: PatternInsightsRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val startDiscoveryPhaseUseCase =
        StartDiscoveryPhaseUseCase(sleepRepository, userPreferencesRepository)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _errors = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val errors: SharedFlow<Int> = _errors.asSharedFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observeUserProfile().collect { profile ->
                val discovery = profile.discoveryPhase?.takeIf { it.isActive }
                val reminderParts = profile.reminderTime.split(":")
                val hour = reminderParts.getOrNull(0)?.toIntOrNull() ?: 18
                val minute = reminderParts.getOrNull(1)?.toIntOrNull() ?: 0
                val today = LocalDate.now()
                val dayNumber = discovery?.let {
                    ChronoUnit.DAYS.between(it.startDate, today).toInt().coerceAtLeast(0) + 1
                } ?: 0
                val weekStart = today.minusDays(6)
                val weekRatings = discovery?.weeklyRatings
                    ?.count { it.rating != null && !it.date.isBefore(weekStart) } ?: 0
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cycleLengthMin = profile.optimalCycleMinutes,
                        sleepLatencyMin = profile.sleepLatencyMinutes,
                        checkInHour = hour,
                        checkInMinute = minute,
                        discoveryPhaseActive = discovery != null,
                        discoveryPhaseCompleted = profile.discoveryPhase?.isCompleted == true,
                        discoveryDayNumber = dayNumber,
                        discoveryCurrentShift = discovery?.getCurrentShift(),
                        discoveryWeekRatingsCount = weekRatings,
                    )
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeDailyCheckInEnabled().collect { enabled ->
                _uiState.update { it.copy(dailyCheckInEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeBedtimeReminderEnabled().collect { enabled ->
                _uiState.update { it.copy(bedtimeReminderEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeMorningRatingEnabled().collect { enabled ->
                _uiState.update { it.copy(morningRatingEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeMorningBedtimeLogEnabled().collect { enabled ->
                _uiState.update { it.copy(morningBedtimeLogEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeSmartWakeWindowEnabled().collect { enabled ->
                _uiState.update { it.copy(smartWakeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.observeFirebaseSyncEnabled().collect { enabled ->
                _uiState.update { it.copy(firebaseSyncEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            patternInsightsRepository.observePatternInsightsEnabled().collect { enabled ->
                _uiState.update { it.copy(patternInsightsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            sleepRepository.getAllSleepLogs().collect { logs ->
                _uiState.update { it.copy(ratingDaysCount = logs.count { log -> log.qualityRating != null }) }
            }
        }
    }

    fun onCycleDurationChanged(minutes: Int) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setCycleDuration(minutes)
            if (result is Result.Error) {
                _errors.tryEmit(result.error.toMessageRes())
            }
        }
    }

    fun onSleepLatencyChanged(minutes: Int) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setSleepLatency(minutes)
            if (result is Result.Error) {
                _errors.tryEmit(result.error.toMessageRes())
            }
        }
    }

    fun onReminderTimeChanged(hour: Int, minute: Int) {
        viewModelScope.launch {
            val time = "%02d:%02d".format(hour, minute)
            val result = userPreferencesRepository.setReminderTime(time)
            if (result is Result.Error) {
                _errors.tryEmit(result.error.toMessageRes())
            }
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
        }
    }

    fun onBedtimeReminderToggled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBedtimeReminderEnabled(enabled)
            // Enabling only persists the preference — the actual reminder is scheduled on the
            // Calculator when a bedtime is chosen (a reminder needs a concrete bedtime). Disabling
            // must cancel any reminder already scheduled from a previous selection.
            if (!enabled) notificationScheduler.cancelBedtimeReminder()
        }
    }

    fun onMorningRatingToggled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setMorningRatingEnabled(enabled)
            val wakeTime = userPreferencesRepository.observeSelectedWakeTime().first()
            if (enabled) notificationScheduler.scheduleMorningFeedback(wakeTime)
            else notificationScheduler.cancelMorningFeedback()
        }
    }

    fun onMorningBedtimeLogToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setMorningBedtimeLogEnabled(enabled) }
    }

    fun onSmartWakeToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setSmartWakeWindowEnabled(enabled) }
    }

    fun onFirebaseSyncToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setFirebaseSyncEnabled(enabled) }
    }

    fun onPatternInsightsToggled(enabled: Boolean) {
        viewModelScope.launch { patternInsightsRepository.setPatternInsightsEnabled(enabled) }
    }

    fun onStartDiscoveryPhase() {
        viewModelScope.launch {
            _uiState.update { it.copy(discoveryStartError = null) }
            when (val result = startDiscoveryPhaseUseCase()) {
                is Result.Success -> Unit
                is Result.Error -> _uiState.update {
                    it.copy(discoveryStartError = result.error.toMessageRes())
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onCancelDiscoveryPhase() {
        viewModelScope.launch { userPreferencesRepository.endDiscoveryPhase() }
    }
}
