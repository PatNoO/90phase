package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class StartDiscoveryPhaseUseCase(
    private val sleepRepository: SleepRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): Result<DiscoveryPhase> {
        val profileResult = userPreferencesRepository.getUserProfile()
        if (profileResult is Result.Error) return profileResult

        val profile = (profileResult as Result.Success).data
        if (profile.isDiscoveryPhaseActive()) {
            return Result.Error(
                DomainError.DiscoveryPhaseAlreadyActive("Discovery phase is already running"),
            )
        }

        val logCount = sleepRepository.getAllSleepLogs().first().size
        if (logCount < MIN_LOGS_REQUIRED) {
            return Result.Error(
                DomainError.InsufficientData(
                    "Need at least $MIN_LOGS_REQUIRED sleep logs to start Discovery Phase (have $logCount)",
                ),
            )
        }

        val phase =
            DiscoveryPhase(
                isActive = true,
                currentShift = ShiftType.LongerLatency,
                startDate = LocalDate.now(),
            )
        return userPreferencesRepository.startDiscoveryPhase(phase).map { phase }
    }

    companion object {
        const val MIN_LOGS_REQUIRED = 7
    }
}
