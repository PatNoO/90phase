package com.example.a90phase.domain.entities

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class BedtimeRecommendation(
    val bedtime: LocalTime,
    val cycleCount: Int,
    val quality: BedtimeQuality,
    val durationMinutes: Int,
) {
    val isOptimal: Boolean get() = quality == BedtimeQuality.OPTIMAL
    val isPassed: Boolean get() = quality == BedtimeQuality.PASSED

    fun formatBedtime(): String = bedtime.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun totalSleepHours(): Double = durationMinutes / 60.0
}

enum class BedtimeQuality {
    OPTIMAL,
    GOOD,
    MINIMAL,
    PASSED,
}
