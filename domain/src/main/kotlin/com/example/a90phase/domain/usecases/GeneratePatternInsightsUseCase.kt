package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.InsightType
import com.example.a90phase.domain.entities.PatternInsight
import com.example.a90phase.domain.entities.SleepLog
import java.time.LocalDate
import kotlin.math.sqrt

private const val MIN_LOGS_REQUIRED = 14
private const val MIN_DAYS_FOR_WEEKDAY_TREND = 3
private const val WEEKDAY_TREND_THRESHOLD = 1.0
private const val CYCLE_TREND_THRESHOLD = 1.0
private const val MIN_BEDTIME_LOGS = 7
private const val MAX_CONSISTENT_STD_DEV_MINUTES = 30.0

class GeneratePatternInsightsUseCase {
    operator fun invoke(
        logs: List<SleepLog>,
        today: LocalDate = LocalDate.now(),
    ): List<PatternInsight> {
        if (logs.size < MIN_LOGS_REQUIRED) return emptyList()
        return listOfNotNull(
            weekdayTrendInsight(logs, today),
            cycleCountTrendInsight(logs, today),
            consistencyInsight(logs, today),
        )
    }

    private fun weekdayTrendInsight(
        logs: List<SleepLog>,
        today: LocalDate,
    ): PatternInsight? {
        val byDay =
            logs
                .filter { it.qualityRating != null }
                .groupBy { it.date.dayOfWeek }
                .mapValues { (_, dayLogs) -> dayLogs.mapNotNull { it.qualityRating }.average() }
        if (byDay.size < MIN_DAYS_FOR_WEEKDAY_TREND) return null
        val best = byDay.maxByOrNull { it.value } ?: return null
        val worst = byDay.minByOrNull { it.value } ?: return null
        if (best.value - worst.value < WEEKDAY_TREND_THRESHOLD) return null
        val dayName =
            best.key.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        return PatternInsight(
            id = "${InsightType.WEEKDAY_TREND.name}-$today",
            message = "You tend to sleep better on ${dayName}s.",
            type = InsightType.WEEKDAY_TREND,
            createdAt = today,
        )
    }

    private fun cycleCountTrendInsight(
        logs: List<SleepLog>,
        today: LocalDate,
    ): PatternInsight? {
        val byCycles =
            logs
                .filter { it.qualityRating != null }
                .groupBy { it.cycleCount }
                .mapValues { (_, cycleLogs) -> cycleLogs.mapNotNull { it.qualityRating }.average() }
        if (byCycles.size < 2) return null
        val best = byCycles.maxByOrNull { it.value } ?: return null
        val worst = byCycles.minByOrNull { it.value } ?: return null
        if (best.value - worst.value < CYCLE_TREND_THRESHOLD) return null
        val totalMinutes = best.key * best.key.let { logs.first().cycleDurationUsed }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val durationStr = if (minutes == 0) "${hours}h" else "${hours}h ${minutes}min"
        return PatternInsight(
            id = "${InsightType.CYCLE_COUNT_TREND.name}-$today",
            message = "You tend to sleep better with ${best.key} sleep cycles ($durationStr).",
            type = InsightType.CYCLE_COUNT_TREND,
            createdAt = today,
        )
    }

    private fun consistencyInsight(
        logs: List<SleepLog>,
        today: LocalDate,
    ): PatternInsight? {
        val bedtimeMinutes =
            logs.mapNotNull { log ->
                log.bedtime?.let { (it.epochSecond % SECONDS_IN_DAY).toInt() / SECONDS_IN_MINUTE }
            }
        if (bedtimeMinutes.size < MIN_BEDTIME_LOGS) return null
        val mean = bedtimeMinutes.average()
        val variance = bedtimeMinutes.map { (it - mean) * (it - mean) }.average()
        if (sqrt(variance) > MAX_CONSISTENT_STD_DEV_MINUTES) return null
        return PatternInsight(
            id = "${InsightType.CONSISTENCY.name}-$today",
            message = "Your bedtimes have been very consistent this period!",
            type = InsightType.CONSISTENCY,
            createdAt = today,
        )
    }

    private companion object {
        const val SECONDS_IN_DAY = 86400L
        const val SECONDS_IN_MINUTE = 60
    }
}
