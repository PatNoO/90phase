package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.AnalyzeDiscoveryPhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoveryResultsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val analyzeUseCase = AnalyzeDiscoveryPhaseUseCase(userPreferencesRepository)
    private var isApplied = false

    private val _uiState = MutableStateFlow<DiscoveryResultsUiState>(DiscoveryResultsUiState.Loading)
    val uiState: StateFlow<DiscoveryResultsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.observeUserProfile().collect { profile ->
                if (isApplied) return@collect
                val phase = profile.discoveryPhase ?: run {
                    _uiState.value = DiscoveryResultsUiState.Error("No discovery phase data available")
                    return@collect
                }
                val averageByShift = phase.weeklyRatings
                    .filter { it.rating != null }
                    .groupBy { it.shiftType }
                    .mapValues { (_, ratings) -> ratings.mapNotNull { it.rating }.average() }
                val winningShift = averageByShift.entries
                    .sortedWith(
                        compareByDescending<Map.Entry<ShiftType, Double>> { it.value }
                            .thenBy { if (it.key is ShiftType.LongerCycles) 0 else 1 },
                    )
                    .firstOrNull()?.key ?: phase.currentShift
                _uiState.value = DiscoveryResultsUiState.Ready(
                    winningShift = winningShift,
                    averageRatings = averageByShift,
                    previousCycleDuration = profile.optimalCycleMinutes,
                    previousSleepLatency = profile.sleepLatencyMinutes,
                    previousCycleCount = profile.preferredCycleCount,
                    newCycleDuration = winningShift.getCycleDuration(),
                    newSleepLatency = winningShift.getSleepLatency(),
                    newCycleCount = winningShift.getCycleCount(),
                )
            }
        }
    }

    fun onApply() {
        viewModelScope.launch {
            when (val result = analyzeUseCase()) {
                is Result.Success -> {
                    isApplied = true
                    _uiState.update { state ->
                        if (state is DiscoveryResultsUiState.Ready) state.copy(isApplied = true) else state
                    }
                }
                is Result.Error -> _uiState.update { state ->
                    if (state is DiscoveryResultsUiState.Ready) {
                        state.copy(applyError = result.error.message ?: "Failed to apply results")
                    } else {
                        state
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }
}
