package com.example.a90phase.domain

import com.example.a90phase.domain.entities.SmartWakeWindow
import com.example.a90phase.domain.usecases.ScheduleSmartWakeUseCase
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class ScheduleSmartWakeUseCaseTest {
    private val useCase = ScheduleSmartWakeUseCase()

    // ── Standard window ────────────────────────────────────────────────────────

    @Test
    fun `windowStart is 30 minutes before wakeTime by default`() {
        val wakeTime = Instant.parse("2024-01-15T07:00:00Z")
        val window = SmartWakeWindow(isEnabled = true, wakeTime = wakeTime)

        val schedule = useCase(window)

        schedule.windowStart shouldBe Instant.parse("2024-01-15T06:30:00Z")
    }

    @Test
    fun `windowEnd is wakeTime when windowEndMinutes is 0`() {
        val wakeTime = Instant.parse("2024-01-15T07:00:00Z")
        val window = SmartWakeWindow(isEnabled = true, wakeTime = wakeTime)

        val schedule = useCase(window)

        schedule.windowEnd shouldBe wakeTime
    }

    @Test
    fun `windowStart and windowEnd reflect custom minutes`() {
        val wakeTime = Instant.parse("2024-01-15T07:00:00Z")
        val window =
            SmartWakeWindow(
                isEnabled = true,
                wakeTime = wakeTime,
                windowStartMinutes = 45,
                windowEndMinutes = 10,
            )

        val schedule = useCase(window)

        schedule.windowStart shouldBe Instant.parse("2024-01-15T06:15:00Z")
        schedule.windowEnd shouldBe Instant.parse("2024-01-15T06:50:00Z")
    }

    // ── Midnight crossing ──────────────────────────────────────────────────────

    @Test
    fun `windowStart crosses midnight correctly`() {
        val wakeTime = Instant.parse("2024-01-15T00:15:00Z")
        val window = SmartWakeWindow(isEnabled = true, wakeTime = wakeTime)

        val schedule = useCase(window)

        schedule.windowStart shouldBe Instant.parse("2024-01-14T23:45:00Z")
    }

    @Test
    fun `windowEnd crossing midnight handled correctly`() {
        val wakeTime = Instant.parse("2024-01-15T00:05:00Z")
        val window =
            SmartWakeWindow(
                isEnabled = true,
                wakeTime = wakeTime,
                windowStartMinutes = 30,
                windowEndMinutes = 10,
            )

        val schedule = useCase(window)

        schedule.windowStart shouldBe Instant.parse("2024-01-14T23:35:00Z")
        schedule.windowEnd shouldBe Instant.parse("2024-01-14T23:55:00Z")
    }
}
