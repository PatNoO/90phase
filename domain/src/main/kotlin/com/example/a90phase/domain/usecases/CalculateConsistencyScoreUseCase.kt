package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.ConsistencyScore
import com.example.a90phase.domain.entities.SleepLog
import kotlin.math.sqrt

class CalculateConsistencyScoreUseCase {
    operator fun invoke(logs: List<SleepLog>): ConsistencyScore? {
        val bedtimes =
            logs
                .sortedByDescending { it.date }
                .take(7)
                .mapNotNull {
                    it.bedtime
                        ?.epochSecond
                        ?.rem(SECONDS_IN_DAY)
                        ?.toDouble()
                }
        if (bedtimes.size < MIN_LOGS) return null
        val mean = bedtimes.average()
        val variance = bedtimes.map { d -> (d - mean) * (d - mean) }.average()
        val stdDev = sqrt(variance)
        val percentage = ((1.0 - stdDev / MAX_STD_DEV_SECONDS) * 100).toInt().coerceIn(0, 100)
        val label =
            when {
                percentage >= 70 -> ConsistencyScore.Label.HIGH
                percentage >= 40 -> ConsistencyScore.Label.MEDIUM
                else -> ConsistencyScore.Label.LOW
            }
        return ConsistencyScore(percentage = percentage, label = label)
    }

    private companion object {
        const val MIN_LOGS = 3
        const val SECONDS_IN_DAY = 86400L
        const val MAX_STD_DEV_SECONDS = 3600.0
    }
}
