package com.example.a90phase.presentation.viewmodels

import androidx.annotation.StringRes
import com.example.a90phase.domain.entities.SleepLog

sealed class LogDetailUiState {
    data object Loading : LogDetailUiState()

    data class Content(val log: SleepLog) : LogDetailUiState()

    data object NotFound : LogDetailUiState()

    data class Error(@StringRes val messageRes: Int) : LogDetailUiState()
}
