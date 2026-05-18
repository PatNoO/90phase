package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import java.time.LocalDate
import java.time.LocalTime

class CalculateOptimalBedtimeUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend operator fun invoke(
        wakeUpTime: LocalTime,
        currentTime: LocalTime = LocalTime.now(),
        today: LocalDate = LocalDate.now(),
    ): Result<List<BedtimeRecommendation>> {
        val profileResult = userPreferencesRepository.getUserProfile()
        if (profileResult is Result.Error) {
            return Result.Error(
                DomainError.CalculationFailed(
                    "Could not load user profile: ${profileResult.error.message}",
                ),
            )
        }
        val profile = (profileResult as Result.Success).data
        val activeShift = profile.discoveryPhase?.takeIf { it.isActive }?.getCurrentShift(today)
        val cycleDuration = activeShift?.getCycleDuration() ?: profile.optimalCycleMinutes
        val sleepLatency = activeShift?.getSleepLatency() ?: profile.sleepLatencyMinutes

        val recommendations =
            CYCLE_COUNTS.map { cycles ->
                val totalMinutes = (cycles * cycleDuration) + sleepLatency
                val bedtime = wakeUpTime.minusMinutes(totalMinutes.toLong())
                val quality =
                    when {
                        isPassed(bedtime, currentTime) -> BedtimeQuality.PASSED
                        cycles == 6 -> BedtimeQuality.OPTIMAL
                        cycles == 5 -> BedtimeQuality.GOOD
                        else -> BedtimeQuality.MINIMAL
                    }
                BedtimeRecommendation(
                    bedtime = bedtime,
                    cycleCount = cycles,
                    quality = quality,
                    durationMinutes = totalMinutes,
                )
            }

        return Result.Success(recommendations)
    }

    // A bedtime is "passed" only if it is within the last 12 hours behind currentTime.
    // Bedtimes more than 12 hours "before" are actually future times crossing midnight
    // (e.g. 00:45 when current time is 22:00 is tonight's future bedtime, not yesterday's).
    private fun isPassed(
        bedtime: LocalTime,
        currentTime: LocalTime,
    ): Boolean {
        val diffSeconds = currentTime.toSecondOfDay() - bedtime.toSecondOfDay()
        return diffSeconds in 1..TWELVE_HOURS_SECONDS
    }

    companion object {
        val CYCLE_COUNTS = listOf(6, 5, 4)
        private const val TWELVE_HOURS_SECONDS = 43_200
    }
}
