package com.example.a90phase.domain

import com.example.a90phase.domain.entities.InsightType
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.usecases.GeneratePatternInsightsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class GeneratePatternInsightsUseCaseTest {
    private val useCase = GeneratePatternInsightsUseCase()
    private val today = LocalDate.of(2025, 6, 9)

    // ── Minimum data requirement ──────────────────────────────────────────────

    @Test
    fun `returns empty when fewer than 14 logs`() {
        val result = useCase(buildLogs(13), today)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns results when exactly 14 logs are provided`() {
        val logs = buildLogsWithWeekdayPattern()
        val result = useCase(logs, today)
        assertTrue(result.isNotEmpty())
    }

    // ── Weekday trend ─────────────────────────────────────────────────────────

    @Test
    fun `produces weekday trend when one day has notably higher ratings`() {
        val logs = buildLogsWithWeekdayPattern()
        val result = useCase(logs, today)
        val insight = result.find { it.type == InsightType.WEEKDAY_TREND }
        assertTrue("Expected WEEKDAY_TREND insight", insight != null)
        assertTrue(insight!!.message.contains("Monday"))
    }

    @Test
    fun `no weekday trend when ratings are similar across all days`() {
        val logs = buildUniformLogs(rating = 3)
        val result = useCase(logs, today)
        assertTrue(result.none { it.type == InsightType.WEEKDAY_TREND })
    }

    @Test
    fun `weekday trend insight id includes type and date`() {
        val logs = buildLogsWithWeekdayPattern()
        val result = useCase(logs, today)
        val insight = result.find { it.type == InsightType.WEEKDAY_TREND }
        assertEquals("WEEKDAY_TREND-$today", insight?.id)
    }

    // ── Cycle count trend ─────────────────────────────────────────────────────

    @Test
    fun `produces cycle count trend when one cycle count correlates with better sleep`() {
        val logs = buildLogsWithCycleTrend()
        val result = useCase(logs, today)
        val insight = result.find { it.type == InsightType.CYCLE_COUNT_TREND }
        assertTrue("Expected CYCLE_COUNT_TREND insight", insight != null)
        assertTrue(insight!!.message.contains("6 sleep cycles"))
    }

    @Test
    fun `no cycle count trend when all logs have same cycle count`() {
        val logs = buildUniformLogs(cycleCount = 6)
        val result = useCase(logs, today)
        assertTrue(result.none { it.type == InsightType.CYCLE_COUNT_TREND })
    }

    // ── Consistency insight ───────────────────────────────────────────────────

    @Test
    fun `produces consistency insight when bedtimes have low variance`() {
        val bedtime = Instant.parse("2025-06-01T22:00:00Z")
        val logs =
            (0 until 14).map { i ->
                buildLog(
                    id = "c$i",
                    date = today.minusDays(i.toLong()),
                    rating = 4,
                    bedtime = bedtime.plusSeconds(i * 300L),
                )
            }
        val result = useCase(logs, today)
        val insight = result.find { it.type == InsightType.CONSISTENCY }
        assertTrue("Expected CONSISTENCY insight", insight != null)
    }

    @Test
    fun `no consistency insight when fewer than 7 logs have bedtimes`() {
        val logs =
            (0 until 14).map { i ->
                buildLog(id = "n$i", date = today.minusDays(i.toLong()), rating = 4, bedtime = null)
            }
        val result = useCase(logs, today)
        assertTrue(result.none { it.type == InsightType.CONSISTENCY })
    }

    @Test
    fun `no consistency insight when bedtimes vary widely`() {
        val logs =
            (0 until 14).map { i ->
                buildLog(
                    id = "v$i",
                    date = today.minusDays(i.toLong()),
                    rating = 4,
                    bedtime = Instant.parse("2025-06-01T22:00:00Z").plusSeconds(i * 3600L),
                )
            }
        val result = useCase(logs, today)
        assertTrue(result.none { it.type == InsightType.CONSISTENCY })
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildLog(
        id: String,
        date: LocalDate,
        rating: Int? = 3,
        cycleCount: Int = 6,
        bedtime: Instant? = null,
    ) = SleepLog(
        id = id,
        date = date,
        wakeTime = Instant.parse("2025-06-09T06:30:00Z"),
        qualityRating = rating,
        cycleCount = cycleCount,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
        syncStatus = SyncStatus.SYNCED,
        bedtime = bedtime,
    )

    private fun buildLogs(count: Int): List<SleepLog> =
        (0 until count).map { i ->
            buildLog(id = "$i", date = today.minusDays(i.toLong()))
        }

    private fun buildUniformLogs(
        rating: Int = 3,
        cycleCount: Int = 6,
    ): List<SleepLog> =
        (0 until 14).map { i ->
            buildLog(id = "u$i", date = today.minusDays(i.toLong()), rating = rating, cycleCount = cycleCount)
        }

    private fun buildLogsWithWeekdayPattern(): List<SleepLog> {
        val base = LocalDate.of(2025, 6, 2)
        return (0 until 14).map { i ->
            val date = base.plusDays(i.toLong())
            val rating = if (date.dayOfWeek == DayOfWeek.MONDAY) 5 else 2
            buildLog(id = "w$i", date = date, rating = rating)
        }
    }

    private fun buildLogsWithCycleTrend(): List<SleepLog> =
        (0 until 14).map { i ->
            val cycles = if (i % 2 == 0) 6 else 4
            val rating = if (cycles == 6) 5 else 2
            buildLog(id = "cc$i", date = today.minusDays(i.toLong()), rating = rating, cycleCount = cycles)
        }
}
