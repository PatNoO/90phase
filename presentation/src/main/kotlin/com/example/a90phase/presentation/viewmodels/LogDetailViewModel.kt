package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.usecases.DeleteSleepLogUseCase
import com.example.a90phase.domain.usecases.GetSleepLogUseCase
import com.example.a90phase.presentation.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sleepRepository: SleepRepository,
) : ViewModel() {

    private val logId: String = checkNotNull(savedStateHandle[Route.LogDetail.ARG_LOG_ID])

    private val getSleepLogUseCase = GetSleepLogUseCase(sleepRepository)
    private val deleteSleepLogUseCase = DeleteSleepLogUseCase(sleepRepository)

    private val _events = MutableSharedFlow<LogDetailEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LogDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<LogDetailUiState> = getSleepLogUseCase(logId)
        .map { log -> if (log != null) LogDetailUiState.Content(log) else LogDetailUiState.NotFound }
        .catch { emit(LogDetailUiState.Error("Could not load this sleep log")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LogDetailUiState.Loading,
        )

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            deleteSleepLogUseCase(logId)
                .onSuccess { _events.tryEmit(LogDetailEvent.Deleted) }
                .onError { _events.tryEmit(LogDetailEvent.DeleteFailed("Could not delete this sleep log")) }
        }
    }
}

sealed class LogDetailEvent {
    data object Deleted : LogDetailEvent()

    data class DeleteFailed(val message: String) : LogDetailEvent()
}
