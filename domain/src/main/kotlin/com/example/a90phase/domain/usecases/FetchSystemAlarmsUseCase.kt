package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.repositories.AlarmRepository

class FetchSystemAlarmsUseCase(
    private val alarmRepository: AlarmRepository,
) {
    suspend operator fun invoke(): Result<List<SystemAlarm>> =
        when (val result = alarmRepository.getAllAlarms()) {
            is Result.Success -> result
            is Result.Error ->
                when (result.error) {
                    is DomainError.PermissionDenied -> Result.Success(emptyList())
                    else -> result
                }
            is Result.Loading -> result
        }
}
