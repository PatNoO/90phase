package com.example.a90phase.data.local.room.mapper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SleepLogMapperTest {

    private val baseInstant = Instant.parse("2026-05-15T06:00:00Z")
    private val baseDate = LocalDate.of(2026, 5, 15)

    @Test
    fun sleepLog_roundTrip_preservesAllFields() {
        val original = SleepLog(
            id = "log-1",
            date = baseDate,
            bedtime = Instant.parse("2026-05-14T22:30:00Z"),
            wakeTime = baseInstant,
            qualityRating = 4,
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            notes = "Felt rested",
            createdAt = baseInstant,
            updatedAt = baseInstant,
            syncStatus = SyncStatus.PENDING_UPLOAD,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.date, roundTripped.date)
        assertEquals(original.bedtime, roundTripped.bedtime)
        assertEquals(original.wakeTime, roundTripped.wakeTime)
        assertEquals(original.qualityRating, roundTripped.qualityRating)
        assertEquals(original.cycleCount, roundTripped.cycleCount)
        assertEquals(original.cycleDurationUsed, roundTripped.cycleDurationUsed)
        assertEquals(original.sleepLatencyUsed, roundTripped.sleepLatencyUsed)
        assertEquals(original.notes, roundTripped.notes)
        assertEquals(original.createdAt, roundTripped.createdAt)
        assertEquals(original.updatedAt, roundTripped.updatedAt)
        assertEquals(original.syncStatus, roundTripped.syncStatus)
    }

    @Test
    fun sleepLog_roundTrip_withNullableFieldsNull() {
        val original = SleepLog(
            id = "log-2",
            date = baseDate,
            bedtime = null,
            wakeTime = baseInstant,
            qualityRating = null,
            cycleCount = 5,
            cycleDurationUsed = 105,
            sleepLatencyUsed = 30,
            notes = null,
            createdAt = baseInstant,
            updatedAt = baseInstant,
            syncStatus = SyncStatus.SYNCED,
        )

        val roundTripped = original.toEntity().toDomain()

        assertNull(roundTripped.bedtime)
        assertNull(roundTripped.qualityRating)
        assertNull(roundTripped.notes)
        assertEquals(SyncStatus.SYNCED, roundTripped.syncStatus)
    }
}
