package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.usecases.FetchSystemAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    alarmRepository: AlarmRepository,
) : ViewModel() {

    private val fetchSystemAlarmsUseCase = FetchSystemAlarmsUseCase(alarmRepository)

    private val _nextAlarm = MutableStateFlow<SystemAlarm?>(null)
    val nextAlarm: StateFlow<SystemAlarm?> = _nextAlarm.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = fetchSystemAlarmsUseCase()) {
                is Result.Success -> _nextAlarm.value = result.data.firstOrNull()
                is Result.Error, is Result.Loading -> _nextAlarm.value = null
            }
        }
    }
}
