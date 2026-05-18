package com.example.a90phase.domain.entities

import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

data class DiscoveryPhase(
    val isActive: Boolean,
    val currentShift: ShiftType,
    val startDate: LocalDate,
    val weeklyRatings: List<DailyRating> = emptyList(),
    val isCompleted: Boolean = false,
) {
    val endDate: LocalDate = startDate.plusDays(DISCOVERY_DURATION_DAYS.toLong())

    fun getDaysRemaining(): Int {
        val now = LocalDate.now()
        return if (now.isBefore(endDate)) Period.between(now, endDate).days else 0
    }

    fun getCurrentShift(today: LocalDate = LocalDate.now()): ShiftType {
        val daysPassed = ChronoUnit.DAYS.between(startDate, today).toInt()
        return when {
            daysPassed < WEEK_DAYS -> ShiftType.LongerLatency
            daysPassed < WEEK_DAYS * 2 -> ShiftType.LongerCycles
            else -> ShiftType.FewerCycles
        }
    }

    fun hasEnoughData(): Boolean = weeklyRatings.size >= MIN_RATINGS_REQUIRED

    fun getAverageRating(): Double? = weeklyRatings.mapNotNull { it.rating }.takeIf { it.isNotEmpty() }?.average()

    companion object {
        const val DISCOVERY_DURATION_DAYS = 21
        const val MIN_RATINGS_REQUIRED = 7
        private const val WEEK_DAYS = 7
    }
}

sealed class ShiftType(
    val displayName: String,
) {
    data object LongerLatency : ShiftType("Longer sleep onset (30 min)")

    data object LongerCycles : ShiftType("Longer cycles (105 min)")

    data object FewerCycles : ShiftType("Fewer cycles (5)")

    fun getCycleDuration(): Int =
        when (this) {
            is LongerCycles -> 105
            else -> 90
        }

    fun getSleepLatency(): Int =
        when (this) {
            is LongerLatency -> 30
            else -> 15
        }

    fun getCycleCount(): Int =
        when (this) {
            is FewerCycles -> 5
            else -> 6
        }
}

data class DailyRating(
    val date: LocalDate,
    val rating: Int? = null,
    val shiftType: ShiftType,
    val actualWakeUpTime: Instant? = null,
)
