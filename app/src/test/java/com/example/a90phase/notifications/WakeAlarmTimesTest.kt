package com.example.a90phase.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the time arithmetic behind the daily wake alarm, in particular the guard that stops a
 * just-fired alarm from re-arming itself milliseconds later.
 */
class WakeAlarmTimesTest {

    private val wakeTime = LocalTime.of(7, 0)

    // ── Plain scheduling ───────────────────────────────────────────────────────

    @Test
    fun `schedules today when the wake time is still ahead`() {
        val now = LocalDateTime.of(2025, 5, 15, 22, 30)
        // 07:00 on the 15th has passed by 22:30, so the next occurrence is the 16th.
        assertEquals(LocalDateTime.of(2025, 5, 16, 7, 0), nextWakeOccurrence(wakeTime, now))
    }

    @Test
    fun `schedules later today when the wake time has not passed`() {
        val now = LocalDateTime.of(2025, 5, 15, 3, 0)
        assertEquals(LocalDateTime.of(2025, 5, 15, 7, 0), nextWakeOccurrence(wakeTime, now))
    }

    @Test
    fun `rolls to tomorrow when now is exactly the wake time`() {
        val now = LocalDateTime.of(2025, 5, 15, 7, 0)
        assertEquals(LocalDateTime.of(2025, 5, 16, 7, 0), nextWakeOccurrence(wakeTime, now))
    }

    // ── Re-arm guard ───────────────────────────────────────────────────────────

    @Test
    fun `re-arm rolls to tomorrow when the alarm fires a hair early`() {
        // The receiver can run marginally before the trigger instant. Without the guard this
        // would re-arm for 07:00 today — two milliseconds away — and ring in a loop.
        val now = LocalDateTime.of(2025, 5, 15, 6, 59, 59, 998_000_000)
        val target = nextWakeOccurrence(wakeTime, now, notBefore = now.plusMinutes(1))

        assertEquals(LocalDateTime.of(2025, 5, 16, 7, 0), target)
        assertTrue(target.isAfter(now.plusMinutes(1)))
    }

    @Test
    fun `re-arm rolls to tomorrow when the alarm fires exactly on time`() {
        val now = LocalDateTime.of(2025, 5, 15, 7, 0)
        assertEquals(
            LocalDateTime.of(2025, 5, 16, 7, 0),
            nextWakeOccurrence(wakeTime, now, notBefore = now.plusMinutes(1)),
        )
    }

    @Test
    fun `re-arm after a snooze still targets the next real wake time`() {
        // Snooze fired at 07:09; tomorrow's 07:00 is the next daily occurrence, not 07:00 today.
        val now = LocalDateTime.of(2025, 5, 15, 7, 9)
        assertEquals(
            LocalDateTime.of(2025, 5, 16, 7, 0),
            nextWakeOccurrence(wakeTime, now, notBefore = now.plusMinutes(1)),
        )
    }

    @Test
    fun `re-arm keeps a wake time that is genuinely later today`() {
        // A midday wake time re-armed just after midnight belongs to today, not tomorrow.
        val lateWake = LocalTime.of(13, 0)
        val now = LocalDateTime.of(2025, 5, 15, 0, 5)
        assertEquals(
            LocalDateTime.of(2025, 5, 15, 13, 0),
            nextWakeOccurrence(lateWake, now, notBefore = now.plusMinutes(1)),
        )
    }

    // ── Midnight ───────────────────────────────────────────────────────────────

    @Test
    fun `handles a midnight wake time`() {
        val midnight = LocalTime.MIDNIGHT
        val now = LocalDateTime.of(2025, 5, 15, 23, 50)
        assertEquals(LocalDateTime.of(2025, 5, 16, 0, 0), nextWakeOccurrence(midnight, now))
    }

    @Test
    fun `re-arm across a month boundary`() {
        val now = LocalDateTime.of(2025, 5, 31, 7, 0)
        assertEquals(
            LocalDateTime.of(2025, 6, 1, 7, 0),
            nextWakeOccurrence(wakeTime, now, notBefore = now.plusMinutes(1)),
        )
    }
}
