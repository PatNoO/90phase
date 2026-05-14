package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.SmartWakeWindow
import java.time.Instant

data class SmartWakeSchedule(
    val windowStart: Instant,
    val windowEnd: Instant,
)

class ScheduleSmartWakeUseCase {
    operator fun invoke(window: SmartWakeWindow): SmartWakeSchedule {
        val windowEnd = window.wakeTime.minusSeconds(window.windowEndMinutes * SECONDS_PER_MINUTE)
        val windowStart = window.wakeTime.minusSeconds(window.windowStartMinutes * SECONDS_PER_MINUTE)
        return SmartWakeSchedule(windowStart = windowStart, windowEnd = windowEnd)
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60L
    }
}
