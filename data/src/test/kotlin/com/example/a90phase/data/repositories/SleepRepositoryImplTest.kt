package com.example.a90phase.data.repositories

import app.cash.turbine.test
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.room.dao.SleepLogDao
import com.example.a90phase.data.local.room.entity.SleepLogEntity
import com.example.a90phase.data.sync.SyncScheduler
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** JVM unit tests for SleepRepositoryImpl. */
class SleepRepositoryImplTest {

    private val sleepLogDao = mockk<SleepLogDao>(relaxed = true)
    private val dataStore = mockk<UserPreferencesDataStore>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    private val repo = SleepRepositoryImpl(sleepLogDao, dataStore, syncScheduler)

    private val baseInstant = Instant.parse("2026-05-15T06:00:00Z")
    private val baseDate = LocalDate.of(2026, 5, 15)

    // region read — getAllSleepLogs

    @Test
    fun getAllSleepLogs_mapsEntityListToDomain() = runTest {
        every { sleepLogDao.getSleepLogsFlow() } returns flowOf(listOf(buildEntity("1"), buildEntity("2")))

        repo.getAllSleepLogs().test {
            val logs = awaitItem()
            assertEquals(2, logs.size)
            assertEquals("1", logs[0].id)
            assertEquals("2", logs[1].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllSleepLogs_emptyDao_emitsEmptyList() = runTest {
        every { sleepLogDao.getSleepLogsFlow() } returns flowOf(emptyList())

        repo.getAllSleepLogs().test {
            assertEquals(emptyList<SleepLog>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region read — getSleepLog

    @Test
    fun getSleepLog_mapsEntityToDomain() = runTest {
        every { sleepLogDao.getSleepLogById("42") } returns flowOf(buildEntity("42"))

        repo.getSleepLog("42").test {
            assertEquals("42", awaitItem()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSleepLog_returnsNullForUnknownId() = runTest {
        every { sleepLogDao.getSleepLogById("missing") } returns flowOf(null)

        repo.getSleepLog("missing").test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region read — getSleepLogsByDateRange

    @Test
    fun getSleepLogsByDateRange_delegatesFilterToDao() = runTest {
        val start = LocalDate.of(2026, 5, 10)
        val end = LocalDate.of(2026, 5, 15)
        every { sleepLogDao.getSleepLogsByDateRange(start, end) } returns flowOf(listOf(buildEntity("1")))

        repo.getSleepLogsByDateRange(start, end).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region sync — updateSyncStatus / deleteSleepLog / getPendingUploadLogs

    @Test
    fun updateSyncStatus_delegatesToDao() = runTest {
        val result = repo.updateSyncStatus("1", SyncStatus.SYNCED)

        assertTrue(result is Result.Success)
        coVerify { sleepLogDao.updateSyncStatus("1", SyncStatus.SYNCED) }
    }

    @Test
    fun updateSyncStatus_daoThrows_returnsError() = runTest {
        coEvery { sleepLogDao.updateSyncStatus(any(), any()) } throws RuntimeException("fail")

        val result = repo.updateSyncStatus("1", SyncStatus.SYNCED)

        assertTrue(result is Result.Error)
    }

    @Test
    fun deleteSleepLog_delegatesToDao() = runTest {
        val result = repo.deleteSleepLog("1")

        assertTrue(result is Result.Success)
        coVerify { sleepLogDao.deleteSleepLog("1") }
    }

    @Test
    fun getPendingUploadLogs_returnsMappedDomainList() = runTest {
        coEvery { sleepLogDao.getUnsyncedLogs() } returns listOf(buildEntity("1"), buildEntity("2"))

        val result = repo.getPendingUploadLogs()

        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.size)
        assertEquals("1", result.data[0].id)
    }

    @Test
    fun getPendingUploadLogs_daoThrows_returnsError() = runTest {
        coEvery { sleepLogDao.getUnsyncedLogs() } throws RuntimeException("fail")

        val result = repo.getPendingUploadLogs()

        assertTrue(result is Result.Error)
    }

    // endregion

    // region sync timestamps — DataStore

    @Test
    fun getLastSyncTimestamp_readsEpochMillisFromDataStore() = runTest {
        val epochMillis = 1_716_768_000_000L
        every { dataStore.observeLastSyncTimestamp() } returns flowOf(epochMillis)

        val result = repo.getLastSyncTimestamp()

        assertTrue(result is Result.Success)
        assertEquals(epochMillis, (result as Result.Success).data.toEpochMilli())
    }

    @Test
    fun updateLastSyncTimestamp_writesEpochMillisToDataStore() = runTest {
        val timestamp = Instant.ofEpochMilli(2_000_000L)

        val result = repo.updateLastSyncTimestamp(timestamp)

        assertTrue(result is Result.Success)
        coVerify { dataStore.setLastSyncTimestamp(2_000_000L) }
    }

    // endregion

    // region write — saveSleepLog / updateSleepLog

    @Test
    fun saveSleepLog_writesToDaoAndEnqueuesSync() = runTest {
        val result = repo.saveSleepLog(buildLog("1"))

        assertTrue(result is Result.Success)
        coVerify { sleepLogDao.insertSleepLog(any()) }
        verify { syncScheduler.enqueueSleepLogSync() }
    }

    @Test
    fun updateSleepLog_writesToDaoAndEnqueuesSync() = runTest {
        val result = repo.updateSleepLog(buildLog("1"))

        assertTrue(result is Result.Success)
        coVerify { sleepLogDao.insertSleepLog(any()) }
        verify { syncScheduler.enqueueSleepLogSync() }
    }

    // endregion

    // region error — DAO throws on read

    @Test
    fun saveSleepLog_daoThrows_returnsError() = runTest {
        coEvery { sleepLogDao.insertSleepLog(any()) } throws RuntimeException("DB write failed")

        val result = repo.saveSleepLog(buildLog("1"))

        assertTrue(result is Result.Error)
    }

    // endregion

    private fun buildLog(id: String) = SleepLog(
        id = id,
        date = baseDate,
        wakeTime = baseInstant,
        cycleCount = 6,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
        createdAt = baseInstant,
        updatedAt = baseInstant,
    )

    private fun buildEntity(id: String) = SleepLogEntity(
        id = id,
        date = baseDate,
        bedtime = null,
        wakeTime = baseInstant,
        qualityRating = null,
        cycleCount = 6,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
        notes = null,
        createdAt = baseInstant,
        updatedAt = baseInstant,
        syncStatus = SyncStatus.PENDING_UPLOAD,
    )
}
