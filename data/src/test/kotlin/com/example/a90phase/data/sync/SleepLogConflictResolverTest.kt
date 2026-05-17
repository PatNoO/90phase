package com.example.a90phase.data.sync

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepLogConflictResolverTest {

    @Test
    fun `local wins when local is newer`() {
        val local = Instant.parse("2024-01-02T10:00:00Z")
        val remote = Instant.parse("2024-01-01T10:00:00Z")

        val result = SleepLogConflictResolver.resolve(local, remote)

        assertEquals(ConflictResolution.LOCAL_WINS, result)
    }

    @Test
    fun `remote wins when remote is newer`() {
        val local = Instant.parse("2024-01-01T10:00:00Z")
        val remote = Instant.parse("2024-01-02T10:00:00Z")

        val result = SleepLogConflictResolver.resolve(local, remote)

        assertEquals(ConflictResolution.REMOTE_WINS, result)
    }

    @Test
    fun `local wins on tie to preserve offline-created data`() {
        val timestamp = Instant.parse("2024-01-01T10:00:00Z")

        val result = SleepLogConflictResolver.resolve(timestamp, timestamp)

        assertEquals(ConflictResolution.LOCAL_WINS, result)
    }

    @Test
    fun `local wins when local is 1ms newer`() {
        val local = Instant.parse("2024-01-01T10:00:00.001Z")
        val remote = Instant.parse("2024-01-01T10:00:00.000Z")

        val result = SleepLogConflictResolver.resolve(local, remote)

        assertEquals(ConflictResolution.LOCAL_WINS, result)
    }

    @Test
    fun `remote wins when remote is 1ms newer`() {
        val local = Instant.parse("2024-01-01T10:00:00.000Z")
        val remote = Instant.parse("2024-01-01T10:00:00.001Z")

        val result = SleepLogConflictResolver.resolve(local, remote)

        assertEquals(ConflictResolution.REMOTE_WINS, result)
    }
}
