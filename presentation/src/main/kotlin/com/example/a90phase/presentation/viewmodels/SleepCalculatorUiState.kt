package com.example.a90phase.presentation.viewmodels

import androidx.annotation.StringRes
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.entities.SystemAlarm
import java.time.LocalTime

sealed class SleepCalculatorUiState {
    data object Loading : SleepCalculatorUiState()

    data class Success(
        val wakeTime: LocalTime,
        val bedtimes: List<BedtimeRecommendation>,
        val selectedBedtimeIndex: Int = -1,
        val nextSystemAlarm: SystemAlarm? = null,
        val isSaving: Boolean = false,
        val dailyCheckInEnabled: Boolean = true,
        val alarmActive: Boolean = false,
    ) : SleepCalculatorUiState()

    data class Error(@StringRes val messageRes: Int) : SleepCalculatorUiState()
}
