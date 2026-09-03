package com.example.a90phase.domain

import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class SleepLogTest {
    private fun sampleLog() =
        SleepLog(
            id = "log-1",
            date = LocalDate.of(2026, 5, 14),
            wakeTime = Instant.parse("2026-05-14T05:00:00Z"),
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
        )

    @Test
    fun `default syncStatus is PENDING_UPLOAD`() {
        assertEquals(SyncStatus.PENDING_UPLOAD, sampleLog().syncStatus)
    }

    @Test
    fun `bedtime and qualityRating are null by default`() {
        val log = sampleLog()
        assertNull(log.bedtime)
        assertNull(log.qualityRating)
    }

    @Test
    fun `hasBeenRated returns false when qualityRating is null`() {
        assertFalse(sampleLog().hasBeenRated())
    }

    @Test
    fun `hasBeenRated returns true when qualityRating is set`() {
        val log = sampleLog().copy(qualityRating = 4)
        assertTrue(log.hasBeenRated())
    }

    @Test
    fun `sleepDurationMinutes multiplies cycles by the cycle length used`() {
        val log = sampleLog().copy(cycleCount = 5, cycleDurationUsed = 90)
        assertEquals(450, log.sleepDurationMinutes)
    }

    @Test
    fun `sleepDurationMinutes excludes sleep latency`() {
        val log = sampleLog().copy(cycleCount = 4, cycleDurationUsed = 90, sleepLatencyUsed = 30)
        assertEquals(360, log.sleepDurationMinutes)
    }

    @Test
    fun `sleepDurationMinutes honours a custom cycle length`() {
        val log = sampleLog().copy(cycleCount = 4, cycleDurationUsed = 105)
        assertEquals(420, log.sleepDurationMinutes)
    }

    @Test
    fun `copy preserves immutability`() {
        val original = sampleLog()
        val updated = original.copy(qualityRating = 5, syncStatus = SyncStatus.SYNCED)

        assertNull(original.qualityRating)
        assertEquals(SyncStatus.PENDING_UPLOAD, original.syncStatus)
        assertEquals(5, updated.qualityRating)
        assertEquals(SyncStatus.SYNCED, updated.syncStatus)
    }
}
