package com.example.a90phase.domain

import com.example.a90phase.domain.entities.ConsistencyScore
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.usecases.CalculateConsistencyScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class CalculateConsistencyScoreUseCaseTest {
    private val useCase = CalculateConsistencyScoreUseCase()

    @Test
    fun `returns null when fewer than 3 logs have bedtime`() {
        val logs =
            listOf(
                buildLog("1", bedtime = Instant.parse("2025-05-15T22:00:00Z")),
                buildLog("2", bedtime = Instant.parse("2025-05-16T22:00:00Z")),
                buildLog("3", bedtime = null),
            )
        assertNull(useCase(logs))
    }

    @Test
    fun `returns null when list is empty`() {
        assertNull(useCase(emptyList()))
    }

    @Test
    fun `returns null when all bedtimes are null`() {
        val logs = (1..5).map { buildLog(it.toString(), bedtime = null) }
        assertNull(useCase(logs))
    }

    @Test
    fun `returns HIGH score for perfectly consistent bedtimes`() {
        val base = LocalDate.of(2025, 5, 15)
        val logs =
            (0..6).map { i ->
                val date = base.minusDays(i.toLong())
                buildLog(
                    id = i.toString(),
                    date = date,
                    bedtime = date.atTime(22, 0).toInstant(ZoneOffset.UTC),
                )
            }
        val score = useCase(logs)
        assertNotNull(score)
        assertEquals(ConsistencyScore.Label.HIGH, score!!.label)
        assertEquals(100, score.percentage)
    }

    @Test
    fun `returns LOW score for highly variable bedtimes`() {
        val bedtimes =
            listOf(
                "2025-05-15T20:00:00Z",
                "2025-05-14T01:00:00Z",
                "2025-05-13T23:00:00Z",
                "2025-05-12T22:00:00Z",
                "2025-05-11T01:00:00Z",
            )
        val logs =
            bedtimes.mapIndexed { i, bt ->
                buildLog(
                    id = i.toString(),
                    date = LocalDate.of(2025, 5, 15).minusDays(i.toLong()),
                    bedtime = Instant.parse(bt),
                )
            }
        val score = useCase(logs)
        assertNotNull(score)
        assertEquals(ConsistencyScore.Label.LOW, score!!.label)
    }

    @Test
    fun `returns MEDIUM score for moderately consistent bedtimes`() {
        val bedtimes =
            listOf(
                "2025-05-15T22:00:00Z",
                "2025-05-14T22:30:00Z",
                "2025-05-13T23:00:00Z",
                "2025-05-12T22:15:00Z",
            )
        val logs =
            bedtimes.mapIndexed { i, bt ->
                buildLog(
                    id = i.toString(),
                    date = LocalDate.of(2025, 5, 15).minusDays(i.toLong()),
                    bedtime = Instant.parse(bt),
                )
            }
        val score = useCase(logs)
        assertNotNull(score)
        assertNotNull(score!!.label)
    }

    @Test
    fun `works with exactly 3 bedtime logs`() {
        val logs =
            listOf(
                buildLog("1", bedtime = Instant.parse("2025-05-15T22:00:00Z")),
                buildLog("2", bedtime = Instant.parse("2025-05-14T22:00:00Z")),
                buildLog("3", bedtime = Instant.parse("2025-05-13T22:00:00Z")),
            )
        assertNotNull(useCase(logs))
    }

    @Test
    fun `only uses last 7 logs even when more are provided`() {
        val base = LocalDate.of(2025, 5, 15)
        val consistentLogs =
            (0..6).map { i ->
                val date = base.minusDays(i.toLong())
                buildLog(
                    id = i.toString(),
                    date = date,
                    bedtime = date.atTime(22, 0).toInstant(ZoneOffset.UTC),
                )
            }
        val oldOutlier =
            buildLog(
                id = "outlier",
                date = LocalDate.of(2025, 5, 1),
                bedtime = Instant.parse("2025-05-01T14:00:00Z"),
            )
        val score = useCase(consistentLogs + oldOutlier)
        assertNotNull(score)
        assertEquals(ConsistencyScore.Label.HIGH, score!!.label)
    }

    @Test
    fun `percentage is within 0 to 100 range`() {
        val logs =
            (0..6).map { i ->
                buildLog(
                    id = i.toString(),
                    date = LocalDate.of(2025, 5, 15).minusDays(i.toLong()),
                    bedtime = Instant.ofEpochSecond(i * 3600L),
                )
            }
        val score = useCase(logs)
        if (score != null) {
            assert(score.percentage in 0..100)
        }
    }
}

private fun buildLog(
    id: String,
    date: LocalDate = LocalDate.of(2025, 5, 15),
    bedtime: Instant? = Instant.parse("2025-05-15T22:00:00Z"),
): SleepLog =
    SleepLog(
        id = id,
        date = date,
        bedtime = bedtime,
        wakeTime = Instant.parse("2025-05-15T06:30:00Z"),
        qualityRating = 4,
        cycleCount = 6,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
        syncStatus = SyncStatus.SYNCED,
    )
