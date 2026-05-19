package com.example.a90phase.presentation.viewmodels

data class SettingsUiState(
    val isLoading: Boolean = true,
    val cycleLengthMin: Int = 90,
    val sleepLatencyMin: Int = 15,
    val dailyCheckInEnabled: Boolean = true,
    val checkInHour: Int = 18,
    val checkInMinute: Int = 0,
    val bedtimeReminderEnabled: Boolean = true,
    val morningRatingEnabled: Boolean = false,
    val morningBedtimeLogEnabled: Boolean = false,
    val smartWakeEnabled: Boolean = false,
    val firebaseSyncEnabled: Boolean = true,
    val ratingDaysCount: Int = 0,
    val discoveryPhaseActive: Boolean = false,
    val discoveryDayNumber: Int = 0,
    val discoveryCurrentShiftName: String = "",
    val discoveryWeekRatingsCount: Int = 0,
    val discoveryPhaseCompleted: Boolean = false,
    val discoveryStartError: String? = null,
)
