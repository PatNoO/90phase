package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.entities.ShiftType

sealed class DiscoveryResultsUiState {
    data object Loading : DiscoveryResultsUiState()

    data class Error(val message: String) : DiscoveryResultsUiState()

    data class Ready(
        val winningShift: ShiftType,
        val averageRatings: Map<ShiftType, Double>,
        val previousCycleDuration: Int,
        val previousSleepLatency: Int,
        val previousCycleCount: Int,
        val newCycleDuration: Int,
        val newSleepLatency: Int,
        val newCycleCount: Int,
        val isApplied: Boolean = false,
        val applyError: String? = null,
    ) : DiscoveryResultsUiState()
}
