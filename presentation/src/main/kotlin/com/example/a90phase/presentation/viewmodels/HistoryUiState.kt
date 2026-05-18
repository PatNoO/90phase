package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.entities.SleepLog
import java.time.DayOfWeek

sealed class HistoryUiState {
    data object Loading : HistoryUiState()

    data object Empty : HistoryUiState()

    data class Content(
        val logs: List<SleepLog>,
        val averageRating: Float,
        val totalLogs: Int,
        val bestDay: DayOfWeek?,
    ) : HistoryUiState()

    data class Error(val message: String) : HistoryUiState()
}
