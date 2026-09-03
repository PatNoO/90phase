package com.example.a90phase.domain

import app.cash.turbine.test
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.usecases.DeleteSleepLogUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DeleteSleepLogUseCaseTest {
    @Test
    fun `removes the log with the given id`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1"))
            repo.saveSleepLog(buildLog("id-2"))

            val result = DeleteSleepLogUseCase(repo)("id-1")

            assertTrue(result is Result.Success)
            repo.getAllSleepLogs().test {
                val remaining = awaitItem()
                assertEquals(1, remaining.size)
                assertEquals("id-2", remaining[0].id)
                awaitComplete()
            }
        }

    @Test
    fun `succeeds when the id does not exist`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1"))

            val result = DeleteSleepLogUseCase(repo)("missing")

            assertTrue(result is Result.Success)
            repo.getAllSleepLogs().test {
                assertEquals(1, awaitItem().size)
                awaitComplete()
            }
        }

    @Test
    fun `returns Error when the repository fails`() =
        runTest {
            val result = DeleteSleepLogUseCase(FailingSleepRepository())("id-1")

            assertTrue(result is Result.Error)
        }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun buildLog(id: String) =
        SleepLog(
            id = id,
            date = LocalDate.of(2024, 1, 15),
            wakeTime = Instant.parse("2024-01-15T07:00:00Z"),
            cycleCount = 5,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            syncStatus = SyncStatus.PENDING_UPLOAD,
        )
}
