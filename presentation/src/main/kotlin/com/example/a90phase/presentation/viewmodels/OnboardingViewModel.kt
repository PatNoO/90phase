package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.OnboardingRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.GetOnboardingStateUseCase
import com.example.a90phase.domain.usecases.SaveOnboardingStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ONBOARDING_SCREEN_COUNT = 8

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    private val getOnboardingStateUseCase = GetOnboardingStateUseCase(onboardingRepository)
    private val saveOnboardingStateUseCase = SaveOnboardingStateUseCase(onboardingRepository)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getOnboardingStateUseCase().collect { state ->
                _uiState.update {
                    it.copy(
                        dailyCheckInEnabled = state.dailyCheckInEnabled,
                        bedtimeReminderEnabled = state.bedtimeReminderEnabled,
                        morningRatingEnabled = state.morningRatingEnabled,
                        morningBedtimeLogEnabled = state.morningBedtimeLogEnabled,
                        smartWakeEnabled = state.smartWakeWindowEnabled,
                    )
                }
            }
        }
    }

    fun onNextScreen() {
        val next = _uiState.value.currentScreen + 1
        if (next >= ONBOARDING_SCREEN_COUNT) {
            onOnboardingComplete()
        } else {
            _uiState.update { it.copy(currentScreen = next) }
        }
    }

    fun onWakeTimeSelected(hour: Int, minute: Int) {
        _uiState.update { it.copy(wakeHour = hour, wakeMinute = minute) }
        viewModelScope.launch {
            userPreferencesRepository.setSelectedWakeTime(hour, minute)
        }
    }

    fun onShowWakeTimePicker(show: Boolean) {
        _uiState.update { it.copy(showWakeTimePicker = show) }
    }

    fun onFeatureEnabled(feature: OnboardingFeature, enabled: Boolean) {
        viewModelScope.launch {
            when (feature) {
                OnboardingFeature.DailyCheckIn -> {
                    userPreferencesRepository.setDailyCheckInEnabled(enabled)
                    _uiState.update { it.copy(dailyCheckInEnabled = enabled) }
                    if (enabled) {
                        val reminderTime = userPreferencesRepository.observeUserProfile().first().reminderTime
                        notificationScheduler.scheduleDailyCheckIn(reminderTime)
                    } else {
                        notificationScheduler.cancelDailyCheckIn()
                    }
                }
                OnboardingFeature.BedtimeReminder -> {
                    userPreferencesRepository.setBedtimeReminderEnabled(enabled)
                    _uiState.update { it.copy(bedtimeReminderEnabled = enabled) }
                }
                OnboardingFeature.MorningRating -> {
                    userPreferencesRepository.setMorningRatingEnabled(enabled)
                    _uiState.update { it.copy(morningRatingEnabled = enabled) }
                }
                OnboardingFeature.MorningBedtimeLog -> {
                    userPreferencesRepository.setMorningBedtimeLogEnabled(enabled)
                    _uiState.update { it.copy(morningBedtimeLogEnabled = enabled) }
                }
                OnboardingFeature.SmartWake -> {
                    userPreferencesRepository.setSmartWakeWindowEnabled(enabled)
                    _uiState.update { it.copy(smartWakeEnabled = enabled) }
                }
            }
            persistOnboardingState()
        }
    }

    fun onOnboardingComplete() {
        viewModelScope.launch {
            saveOnboardingStateUseCase.markCompleted()
            _uiState.update { it.copy(isOnboardingComplete = true) }
        }
    }

    private suspend fun persistOnboardingState() {
        val state = _uiState.value
        saveOnboardingStateUseCase(
            UserOnboardingState(
                dailyCheckInEnabled = state.dailyCheckInEnabled,
                bedtimeReminderEnabled = state.bedtimeReminderEnabled,
                morningRatingEnabled = state.morningRatingEnabled,
                morningBedtimeLogEnabled = state.morningBedtimeLogEnabled,
                smartWakeWindowEnabled = state.smartWakeEnabled,
            ),
        )
    }
}
