package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.usecases.LogSleepSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class LogSleepSessionUseCaseTest {
    private val wakeTime = Instant.parse("2024-01-15T07:00:00Z")
    private val bedtime = Instant.parse("2024-01-14T22:00:00Z")
    private val date = LocalDate.of(2024, 1, 15)

    // ── createLog ──────────────────────────────────────────────────────────────

    @Test
    fun `createLog returns Success with saved log`() =
        runTest {
            val repo = FakeSleepRepository()
            val useCase = LogSleepSessionUseCase(repo)

            val result =
                useCase.createLog(
                    date = date,
                    wakeTime = wakeTime,
                    bedtime = bedtime,
                    cycleCount = 5,
                    cycleDuration = 90,
                    sleepLatency = 15,
                )

            assertTrue(result is Result.Success)
            val log = (result as Result.Success).data
            assertEquals(date, log.date)
            assertEquals(wakeTime, log.wakeTime)
            assertEquals(bedtime, log.bedtime)
            assertEquals(5, log.cycleCount)
            assertEquals(90, log.cycleDurationUsed)
            assertEquals(15, log.sleepLatencyUsed)
            assertEquals(SyncStatus.PENDING_UPLOAD, log.syncStatus)
        }

    @Test
    fun `createLog without bedtime returns Success`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val result =
                useCase.createLog(
                    date = date,
                    wakeTime = wakeTime,
                    cycleCount = 6,
                    cycleDuration = 90,
                    sleepLatency = 15,
                )
            val log = (result as Result.Success).data
            assertEquals(null, log.bedtime)
        }

    @Test
    fun `createLog propagates repository error`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FailingSleepRepository())
            val result =
                useCase.createLog(
                    date = date,
                    wakeTime = wakeTime,
                    cycleCount = 6,
                    cycleDuration = 90,
                    sleepLatency = 15,
                )
            assertTrue(result is Result.Error)
        }

    // ── updateRating ───────────────────────────────────────────────────────────

    @Test
    fun `updateRating returns Success for rating in valid range`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val log = buildLog()

            for (rating in 1..5) {
                val result = useCase.updateRating(log, rating)
                assertTrue("rating $rating should succeed", result is Result.Success)
                assertEquals(rating, (result as Result.Success).data.qualityRating)
            }
        }

    @Test
    fun `updateRating returns ValidationError for rating below minimum`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val result = useCase.updateRating(buildLog(), 0)
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.ValidationError)
        }

    @Test
    fun `updateRating returns ValidationError for rating above maximum`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val result = useCase.updateRating(buildLog(), 6)
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.ValidationError)
        }

    @Test
    fun `updateRating sets syncStatus to PENDING_UPLOAD`() =
        runTest {
            val log = buildLog().copy(syncStatus = SyncStatus.SYNCED)
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val updated = (useCase.updateRating(log, 4) as Result.Success).data
            assertEquals(SyncStatus.PENDING_UPLOAD, updated.syncStatus)
        }

    // ── updateBedtime ──────────────────────────────────────────────────────────

    @Test
    fun `updateBedtime returns Success with updated bedtime`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val newBedtime = Instant.parse("2024-01-14T21:30:00Z")
            val result = useCase.updateBedtime(buildLog(), newBedtime)
            assertTrue(result is Result.Success)
            assertEquals(newBedtime, (result as Result.Success).data.bedtime)
        }

    @Test
    fun `updateBedtime sets syncStatus to PENDING_UPLOAD`() =
        runTest {
            val log = buildLog().copy(syncStatus = SyncStatus.SYNCED)
            val useCase = LogSleepSessionUseCase(FakeSleepRepository())
            val updated = (useCase.updateBedtime(log, bedtime) as Result.Success).data
            assertEquals(SyncStatus.PENDING_UPLOAD, updated.syncStatus)
        }

    @Test
    fun `updateBedtime propagates repository error`() =
        runTest {
            val useCase = LogSleepSessionUseCase(FailingSleepRepository())
            val result = useCase.updateBedtime(buildLog(), bedtime)
            assertTrue(result is Result.Error)
        }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun buildLog() =
        SleepLog(
            id = "test-id",
            date = date,
            wakeTime = wakeTime,
            bedtime = bedtime,
            cycleCount = 5,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
        )
}

// ── Test doubles ──────────────────────────────────────────────────────────────

internal class FakeSleepRepository : SleepRepository {
    private val logs = mutableListOf<SleepLog>()

    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        logs.add(log)
        return Result.Success(Unit)
    }

    override fun getAllSleepLogs(): Flow<List<SleepLog>> = flowOf(logs.toList())

    override fun getSleepLog(id: String): Flow<SleepLog?> = flowOf(logs.find { it.id == id })

    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = flowOf(logs.filter { !it.date.isBefore(startDate) && !it.date.isAfter(endDate) })

    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> {
        logs.removeIf { it.id == log.id }
        logs.add(log)
        return Result.Success(Unit)
    }

    override suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteSleepLog(id: String): Result<Unit> {
        logs.removeIf { it.id == id }
        return Result.Success(Unit)
    }

    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> =
        Result.Success(logs.filter { it.syncStatus == SyncStatus.PENDING_UPLOAD })

    override suspend fun getLastSyncTimestamp(): Result<Instant> = Result.Success(Instant.EPOCH)

    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = Result.Success(Unit)
}

internal class FailingSleepRepository : SleepRepository {
    private val error = Result.Error(DomainError.DatabaseError("DB unavailable"))

    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> = error

    override fun getAllSleepLogs(): Flow<List<SleepLog>> = flowOf(emptyList())

    override fun getSleepLog(id: String): Flow<SleepLog?> = flowOf(null)

    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = flowOf(emptyList())

    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> = error

    override suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
    ): Result<Unit> = error

    override suspend fun deleteSleepLog(id: String): Result<Unit> = error

    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> = error

    override suspend fun getLastSyncTimestamp(): Result<Instant> = error

    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = error
}
