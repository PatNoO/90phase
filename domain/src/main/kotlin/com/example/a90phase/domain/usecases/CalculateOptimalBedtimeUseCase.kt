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

        // Anchor everything to real date-times so bedtimes that cross midnight are not
        // mistaken for past times. The wake-up is the next occurrence of wakeUpTime at or
        // after "now"; each bedtime is that wake instant minus the sleep duration.
        val now = today.atTime(currentTime)
        val wakeDateTime = today.atTime(wakeUpTime).let { if (it.isAfter(now)) it else it.plusDays(1) }

        val recommendations =
            CYCLE_COUNTS.map { cycles ->
                val totalMinutes = (cycles * cycleDuration) + sleepLatency
                val bedtimeDateTime = wakeDateTime.minusMinutes(totalMinutes.toLong())
                val quality =
                    when {
                        bedtimeDateTime.isBefore(now) -> BedtimeQuality.PASSED
                        cycles >= 6 -> BedtimeQuality.OPTIMAL
                        cycles == 5 -> BedtimeQuality.GOOD
                        else -> BedtimeQuality.MINIMAL
                    }
                BedtimeRecommendation(
                    bedtime = bedtimeDateTime.toLocalTime(),
                    cycleCount = cycles,
                    quality = quality,
                    durationMinutes = totalMinutes,
                )
            }

        return Result.Success(recommendations)
    }

    companion object {
        // Full cycles offered, longest first. Shorter options (3, 2) keep late-night or
        // poor-sleep users aligned to a cycle boundary instead of waking mid-cycle.
        val CYCLE_COUNTS = listOf(6, 5, 4, 3, 2)
    }
}
