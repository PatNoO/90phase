package com.example.a90phase.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.a90phase.data.local.room.dao.SleepLogDao
import com.example.a90phase.data.local.room.entity.SleepLogEntity
import com.example.a90phase.domain.entities.SyncStatus
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SleepLogDaoTest {

    private lateinit var db: SleepOptimizerDatabase
    private lateinit var dao: SleepLogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SleepOptimizerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sleepLogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertSleepLog_canBeRetrieved() = runTest {
        val entity = buildEntity("1", LocalDate.of(2026, 5, 15))
        dao.insertSleepLog(entity)

        dao.getSleepLogsFlow().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("1", result.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertSleepLog_replacesExistingOnConflict() = runTest {
        val original = buildEntity("1", LocalDate.of(2026, 5, 15), rating = 3)
        val updated = buildEntity("1", LocalDate.of(2026, 5, 15), rating = 5)
        dao.insertSleepLog(original)
        dao.insertSleepLog(updated)

        dao.getSleepLogsFlow().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(5, result.first().qualityRating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSleepLogsFlow_orderedByDateDesc() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 13)))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 15)))
        dao.insertSleepLog(buildEntity("3", LocalDate.of(2026, 5, 14)))

        dao.getSleepLogsFlow().test {
            val result = awaitItem()
            assertEquals(listOf("2", "3", "1"), result.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSleepLogsByDateRange_filtersCorrectly() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 10)))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 13)))
        dao.insertSleepLog(buildEntity("3", LocalDate.of(2026, 5, 16)))

        dao.getSleepLogsByDateRange(
            startDate = LocalDate.of(2026, 5, 12),
            endDate = LocalDate.of(2026, 5, 14),
        ).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("2", result.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUnsyncedLogs_returnsOnlyPendingUpload() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15), syncStatus = SyncStatus.PENDING_UPLOAD))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 14), syncStatus = SyncStatus.SYNCED))
        dao.insertSleepLog(buildEntity("3", LocalDate.of(2026, 5, 13), syncStatus = SyncStatus.PENDING_UPLOAD))

        val result = dao.getUnsyncedLogs()
        assertEquals(2, result.size)
        assertTrue(result.all { it.syncStatus == SyncStatus.PENDING_UPLOAD })
    }

    @Test
    fun markAsSynced_updatesSyncStatusForGivenIds() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15), syncStatus = SyncStatus.PENDING_UPLOAD))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 14), syncStatus = SyncStatus.PENDING_UPLOAD))

        dao.markAsSynced(listOf("1"))

        val unsynced = dao.getUnsyncedLogs()
        assertEquals(1, unsynced.size)
        assertEquals("2", unsynced.first().id)
    }

    @Test
    fun getSleepLogById_returnsMatchingEntry() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15)))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 14)))

        dao.getSleepLogById("1").test {
            assertEquals("1", awaitItem()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getSleepLogById_returnsNullForUnknownId() = runTest {
        dao.getSleepLogById("unknown").test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSyncStatus_changesStatusForId() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15), syncStatus = SyncStatus.PENDING_UPLOAD))

        dao.updateSyncStatus("1", SyncStatus.SYNCED)

        val unsynced = dao.getUnsyncedLogs()
        assertTrue(unsynced.isEmpty())
    }

    @Test
    fun updateSyncStatus_doesNotAffectOtherEntries() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15), syncStatus = SyncStatus.PENDING_UPLOAD))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 14), syncStatus = SyncStatus.PENDING_UPLOAD))

        dao.updateSyncStatus("1", SyncStatus.SYNCED)

        val unsynced = dao.getUnsyncedLogs()
        assertEquals(1, unsynced.size)
        assertEquals("2", unsynced.first().id)
    }

    @Test
    fun deleteSleepLog_removesCorrectEntry() = runTest {
        dao.insertSleepLog(buildEntity("1", LocalDate.of(2026, 5, 15)))
        dao.insertSleepLog(buildEntity("2", LocalDate.of(2026, 5, 14)))

        dao.deleteSleepLog("1")

        dao.getSleepLogsFlow().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("2", result.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun buildEntity(
        id: String,
        date: LocalDate,
        rating: Int? = null,
        syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
    ): SleepLogEntity =
        SleepLogEntity(
            id = id,
            date = date,
            bedtime = null,
            wakeTime = Instant.parse("2026-05-15T06:00:00Z"),
            qualityRating = rating,
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            notes = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            syncStatus = syncStatus,
        )
}
