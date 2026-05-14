package com.example.a90phase.domain

import app.cash.turbine.test
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.usecases.GetSleepHistoryUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class GetSleepHistoryUseCaseTest {
    private fun useCase(repo: FakeSleepRepository = FakeSleepRepository()) = GetSleepHistoryUseCase(repo)

    // ── allLogs ────────────────────────────────────────────────────────────────

    @Test
    fun `allLogs emits empty list when repository has no logs`() =
        runTest {
            useCase().allLogs().test {
                assertTrue(awaitItem().isEmpty())
                awaitComplete()
            }
        }

    @Test
    fun `allLogs emits saved logs`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1", LocalDate.of(2024, 1, 15)))
            repo.saveSleepLog(buildLog("id-2", LocalDate.of(2024, 1, 16)))

            useCase(repo).allLogs().test {
                val items = awaitItem()
                assertEquals(2, items.size)
                awaitComplete()
            }
        }

    // ── logsForDateRange ───────────────────────────────────────────────────────

    @Test
    fun `logsForDateRange returns only logs in range`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1", LocalDate.of(2024, 1, 10)))
            repo.saveSleepLog(buildLog("id-2", LocalDate.of(2024, 1, 15)))
            repo.saveSleepLog(buildLog("id-3", LocalDate.of(2024, 1, 20)))

            useCase(repo)
                .logsForDateRange(
                    LocalDate.of(2024, 1, 12),
                    LocalDate.of(2024, 1, 18),
                ).test {
                    val items = awaitItem()
                    assertEquals(1, items.size)
                    assertEquals("id-2", items[0].id)
                    awaitComplete()
                }
        }

    @Test
    fun `logsForDateRange includes boundary dates`() =
        runTest {
            val repo = FakeSleepRepository()
            val start = LocalDate.of(2024, 1, 10)
            val end = LocalDate.of(2024, 1, 20)
            repo.saveSleepLog(buildLog("start", start))
            repo.saveSleepLog(buildLog("end", end))

            useCase(repo).logsForDateRange(start, end).test {
                val items = awaitItem()
                assertEquals(2, items.size)
                awaitComplete()
            }
        }

    @Test
    fun `logsForDateRange returns empty when no logs in range`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1", LocalDate.of(2024, 1, 1)))

            useCase(repo)
                .logsForDateRange(
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 2, 28),
                ).test {
                    assertTrue(awaitItem().isEmpty())
                    awaitComplete()
                }
        }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun buildLog(
        id: String,
        date: LocalDate,
    ) = SleepLog(
        id = id,
        date = date,
        wakeTime = Instant.parse("2024-01-15T07:00:00Z"),
        cycleCount = 5,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
        syncStatus = SyncStatus.PENDING_UPLOAD,
    )
}
