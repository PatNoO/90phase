package com.example.a90phase.presentation.viewmodels

data class OnboardingUiState(
    val currentScreen: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val showWakeTimePicker: Boolean = false,
    val dailyCheckInEnabled: Boolean = false,
    val bedtimeReminderEnabled: Boolean = false,
    val morningRatingEnabled: Boolean = false,
    val morningBedtimeLogEnabled: Boolean = false,
    val smartWakeEnabled: Boolean = false,
    val isOnboardingComplete: Boolean = false,
)
