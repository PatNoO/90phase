package com.example.a90phase.domain.entities

data class UserOnboardingState(
    val isCompleted: Boolean = false,
    val dailyCheckInEnabled: Boolean = false,
    val bedtimeReminderEnabled: Boolean = false,
    val morningRatingEnabled: Boolean = false,
    val morningBedtimeLogEnabled: Boolean = false,
    val smartWakeWindowEnabled: Boolean = false,
    val discoveryPhaseInfoShown: Boolean = false,
)
