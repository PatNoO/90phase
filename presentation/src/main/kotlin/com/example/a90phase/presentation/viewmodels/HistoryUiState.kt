package com.example.a90phase.presentation.viewmodels

import androidx.annotation.StringRes
import com.example.a90phase.domain.entities.ConsistencyScore
import com.example.a90phase.domain.entities.PatternInsight
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
        val insights: List<PatternInsight> = emptyList(),
        val insightsEnabled: Boolean = false,
        val consistencyScore: ConsistencyScore? = null,
    ) : HistoryUiState()

    data class Error(@StringRes val messageRes: Int) : HistoryUiState()
}
