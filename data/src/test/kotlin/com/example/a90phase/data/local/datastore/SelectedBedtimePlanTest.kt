package com.example.a90phase.data.local.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

/**
 * Covers the wake-time derivation that decides whether a stored bedtime plan belongs to the alarm
 * currently set. Only one plan is ever stored and nothing clears it when the wake time changes, so
 * getting this wrong shows confident but wrong sleep figures on the ringing screen.
 */
class SelectedBedtimePlanTest {

    private fun plan(
        bedtime: LocalTime,
        cycleCount: Int = 5,
        durationMinutes: Int = 465,
    ) = SelectedBedtimePlan(bedtime = bedtime, cycleCount = cycleCount, durationMinutes = durationMinutes)

    // ── wakeTime ───────────────────────────────────────────────────────────────

    @Test
    fun `wake time is bedtime plus duration`() {
        // 21:45 + 9h15 (6 cycles + 15 min latency)
        assertEquals(LocalTime.of(7, 0), plan(LocalTime.of(21, 45), durationMinutes = 555).wakeTime)
    }

    @Test
    fun `wake time wraps past midnight`() {
        // The reported case: 23:15 + 7h45 lands at 07:00 the next morning.
        assertEquals(LocalTime.of(7, 0), plan(LocalTime.of(23, 15), durationMinutes = 465).wakeTime)
    }

    @Test
    fun `wake time handles a bedtime already after midnight`() {
        assertEquals(LocalTime.of(9, 0), plan(LocalTime.of(1, 15), durationMinutes = 465).wakeTime)
    }

    @Test
    fun `wake time handles a full day duration`() {
        assertEquals(LocalTime.of(23, 15), plan(LocalTime.of(23, 15), durationMinutes = 24 * 60).wakeTime)
    }

    // ── belongsTo ──────────────────────────────────────────────────────────────

    @Test
    fun `belongs to the wake time it was built for`() {
        assertTrue(plan(LocalTime.of(23, 15), durationMinutes = 465).belongsTo(LocalTime.of(7, 0)))
    }

    @Test
    fun `does not belong to a different wake time`() {
        // The reported bug: a 07:00 plan shown while the alarm was set for 12:12.
        assertFalse(plan(LocalTime.of(23, 15), durationMinutes = 465).belongsTo(LocalTime.of(12, 12)))
    }

    @Test
    fun `does not belong to a wake time one minute off`() {
        assertFalse(plan(LocalTime.of(23, 15), durationMinutes = 465).belongsTo(LocalTime.of(7, 1)))
    }

    @Test
    fun `belongs across midnight when the wake time matches`() {
        // Bedtime 22:30 + 6h30 = 05:00 next day.
        assertTrue(plan(LocalTime.of(22, 30), durationMinutes = 390).belongsTo(LocalTime.of(5, 0)))
    }

    @Test
    fun `two plans for the same wake time both match`() {
        // 6 cycles from 21:45 and 5 cycles from 23:15 both wake at 07:00.
        val sixCycles = plan(LocalTime.of(21, 45), cycleCount = 6, durationMinutes = 555)
        val fiveCycles = plan(LocalTime.of(23, 15), cycleCount = 5, durationMinutes = 465)
        assertTrue(sixCycles.belongsTo(LocalTime.of(7, 0)))
        assertTrue(fiveCycles.belongsTo(LocalTime.of(7, 0)))
    }
}
