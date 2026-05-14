package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository

class AnalyzeDiscoveryPhaseUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(): Result<UserProfile> {
        val profileResult = userPreferencesRepository.getUserProfile()
        if (profileResult is Result.Error) return profileResult

        val profile = (profileResult as Result.Success).data
        val phase =
            profile.discoveryPhase
                ?: return Result.Error(DomainError.InsufficientData("No active discovery phase found"))

        if (!phase.hasEnoughData()) {
            return Result.Error(
                DomainError.InsufficientData(
                    "Not enough data — need ${DiscoveryPhase.MIN_RATINGS_REQUIRED} ratings",
                ),
            )
        }

        val averageByShift =
            phase.weeklyRatings
                .filter { it.rating != null }
                .groupBy { it.shiftType }
                .mapValues { (_, ratings) -> ratings.mapNotNull { it.rating }.average() }

        if (averageByShift.isEmpty()) {
            return Result.Error(DomainError.InsufficientData("No rated entries found in discovery phase"))
        }

        val bestShift = averageByShift.maxByOrNull { it.value }!!.key
        val updatedProfile =
            profile.copy(
                optimalCycleMinutes = bestShift.getCycleDuration(),
                sleepLatencyMinutes = bestShift.getSleepLatency(),
                preferredCycleCount = bestShift.getCycleCount(),
                discoveryPhase = null,
            )

        return userPreferencesRepository.updateUserProfile(updatedProfile).map { updatedProfile }
    }
}
