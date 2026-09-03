package com.example.a90phase.domain

import app.cash.turbine.test
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.usecases.GetSleepLogUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class GetSleepLogUseCaseTest {
    private fun useCase(repo: FakeSleepRepository = FakeSleepRepository()) = GetSleepLogUseCase(repo)

    @Test
    fun `emits the log matching the id`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1"))
            repo.saveSleepLog(buildLog("id-2"))

            useCase(repo)("id-2").test {
                assertEquals("id-2", awaitItem()?.id)
                awaitComplete()
            }
        }

    @Test
    fun `emits null when no log has that id`() =
        runTest {
            val repo = FakeSleepRepository()
            repo.saveSleepLog(buildLog("id-1"))

            useCase(repo)("missing").test {
                assertNull(awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `emits null when the repository is empty`() =
        runTest {
            useCase()("id-1").test {
                assertNull(awaitItem())
                awaitComplete()
            }
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
