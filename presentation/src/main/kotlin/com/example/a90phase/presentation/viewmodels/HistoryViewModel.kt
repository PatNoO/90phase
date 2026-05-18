package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.usecases.GetSleepHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    sleepRepository: SleepRepository,
) : ViewModel() {

    private val getSleepHistoryUseCase = GetSleepHistoryUseCase(sleepRepository)

    val uiState: StateFlow<HistoryUiState> = getSleepHistoryUseCase.allLogs()
        .map { logs -> toUiState(logs) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading,
        )

    private fun toUiState(logs: List<SleepLog>): HistoryUiState {
        if (logs.isEmpty()) return HistoryUiState.Empty
        val ratings = logs.mapNotNull { it.qualityRating }
        val averageRating = if (ratings.isEmpty()) 0f else ratings.average().toFloat()
        val bestDay = logs.filter { it.qualityRating != null }
            .maxByOrNull { it.qualityRating!! }
            ?.date?.dayOfWeek
        return HistoryUiState.Content(
            logs = logs,
            averageRating = averageRating,
            totalLogs = logs.size,
            bestDay = bestDay,
        )
    }
}
