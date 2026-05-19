package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.StartDiscoveryPhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    sleepRepository: SleepRepository,
) : ViewModel() {

    private val startDiscoveryPhaseUseCase =
        StartDiscoveryPhaseUseCase(sleepRepository, userPreferencesRepository)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
                        discoveryCurrentShiftName = discovery?.getCurrentShift()?.displayName ?: "",
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
            sleepRepository.getAllSleepLogs().collect { logs ->
                _uiState.update { it.copy(ratingDaysCount = logs.count { log -> log.qualityRating != null }) }
            }
        }
    }

    fun onCycleDurationChanged(minutes: Int) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setCycleDuration(minutes)
            if (result is Result.Error) {
                _uiState.update { it.copy(saveError = result.error.message ?: "Save failed") }
            }
        }
    }

    fun onSleepLatencyChanged(minutes: Int) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setSleepLatency(minutes)
            if (result is Result.Error) {
                _uiState.update { it.copy(saveError = result.error.message ?: "Save failed") }
            }
        }
    }

    fun onReminderTimeChanged(hour: Int, minute: Int) {
        viewModelScope.launch {
            val time = "%02d:%02d".format(hour, minute)
            val result = userPreferencesRepository.setReminderTime(time)
            if (result is Result.Error) {
                _uiState.update { it.copy(saveError = result.error.message ?: "Save failed") }
            }
        }
    }

    fun onDailyCheckInToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDailyCheckInEnabled(enabled) }
    }

    fun onBedtimeReminderToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setBedtimeReminderEnabled(enabled) }
    }

    fun onMorningRatingToggled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setMorningRatingEnabled(enabled) }
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

    fun onStartDiscoveryPhase() {
        viewModelScope.launch {
            _uiState.update { it.copy(discoveryStartError = null) }
            when (val result = startDiscoveryPhaseUseCase()) {
                is Result.Success -> Unit
                is Result.Error -> _uiState.update {
                    it.copy(discoveryStartError = result.error.message ?: "Failed to start Discovery Phase")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onCancelDiscoveryPhase() {
        viewModelScope.launch { userPreferencesRepository.endDiscoveryPhase() }
    }

    fun onSaveErrorDismissed() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun onDiscoveryStartErrorDismissed() {
        _uiState.update { it.copy(discoveryStartError = null) }
    }
}
