package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.entities.SleepLog

sealed class LogDetailUiState {
    data object Loading : LogDetailUiState()

    data class Content(val log: SleepLog) : LogDetailUiState()

    data object NotFound : LogDetailUiState()

    data class Error(val message: String) : LogDetailUiState()
}
