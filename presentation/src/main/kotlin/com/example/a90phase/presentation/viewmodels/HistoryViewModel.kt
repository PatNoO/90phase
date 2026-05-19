package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.entities.PatternInsight
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.usecases.CalculateConsistencyScoreUseCase
import com.example.a90phase.domain.usecases.DismissPatternInsightUseCase
import com.example.a90phase.domain.usecases.GeneratePatternInsightsUseCase
import com.example.a90phase.domain.usecases.GetSleepHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    sleepRepository: SleepRepository,
    private val patternInsightsRepository: PatternInsightsRepository,
) : ViewModel() {

    private val getSleepHistoryUseCase = GetSleepHistoryUseCase(sleepRepository)
    private val generatePatternInsightsUseCase = GeneratePatternInsightsUseCase()
    private val dismissPatternInsightUseCase = DismissPatternInsightUseCase(patternInsightsRepository)
    private val calculateConsistencyScoreUseCase = CalculateConsistencyScoreUseCase()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        getSleepHistoryUseCase.allLogs(),
        patternInsightsRepository.observeDismissedInsightIds(),
        patternInsightsRepository.observePatternInsightsEnabled(),
    ) { logs, dismissedIds, insightsEnabled ->
        toUiState(logs, dismissedIds, insightsEnabled)
    }
        .catch { _errors.tryEmit("Databasfel — försök igen") }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading,
        )

    fun onDismissInsight(insightId: String) {
        viewModelScope.launch { dismissPatternInsightUseCase(insightId) }
    }

    private fun toUiState(
        logs: List<SleepLog>,
        dismissedIds: Set<String>,
        insightsEnabled: Boolean,
    ): HistoryUiState {
        if (logs.isEmpty()) return HistoryUiState.Empty
        val ratings = logs.mapNotNull { it.qualityRating }
        val averageRating = if (ratings.isEmpty()) 0f else ratings.average().toFloat()
        val bestDay = logs.filter { it.qualityRating != null }
            .maxByOrNull { it.qualityRating!! }
            ?.date?.dayOfWeek
        val insights = buildInsights(logs, dismissedIds, insightsEnabled)
        val consistencyScore = if (insightsEnabled) calculateConsistencyScoreUseCase(logs) else null
        return HistoryUiState.Content(
            logs = logs,
            averageRating = averageRating,
            totalLogs = logs.size,
            bestDay = bestDay,
            insights = insights,
            insightsEnabled = insightsEnabled,
            consistencyScore = consistencyScore,
        )
    }

    private fun buildInsights(
        logs: List<SleepLog>,
        dismissedIds: Set<String>,
        insightsEnabled: Boolean,
    ): List<PatternInsight> {
        if (!insightsEnabled) return emptyList()
        return generatePatternInsightsUseCase(logs).filter { it.id !in dismissedIds }
    }
}
