package com.example.a90phase.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.a90phase.data.local.room.dao.UserProfileDao
import com.example.a90phase.data.local.room.entity.UserProfileEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileDaoTest {

    private lateinit var db: SleepOptimizerDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SleepOptimizerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userProfileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getUserProfile_returnsNullWhenEmpty() = runTest {
        assertNull(dao.getUserProfile())
    }

    @Test
    fun insertOrUpdateProfile_canBeRetrievedViaSuspend() = runTest {
        dao.insertOrUpdateProfile(buildProfile(userId = "user-1"))
        val result = dao.getUserProfile()
        assertEquals("user-1", result?.userId)
    }

    @Test
    fun getUserProfileFlow_emitsNullThenValueOnInsert() = runTest {
        dao.getUserProfileFlow().test {
            assertNull(awaitItem())
            dao.insertOrUpdateProfile(buildProfile(userId = "user-1"))
            assertEquals("user-1", awaitItem()?.userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertOrUpdateProfile_upsertDoesNotCreateDuplicateRow() = runTest {
        dao.insertOrUpdateProfile(buildProfile(userId = "user-1", email = "a@a.com"))
        dao.insertOrUpdateProfile(buildProfile(userId = "user-1", email = "b@b.com"))

        val result = dao.getUserProfile()
        assertEquals("b@b.com", result?.email)
    }

    @Test
    fun updateDiscoveryPhase_setsJsonOnExistingRow() = runTest {
        dao.insertOrUpdateProfile(buildProfile(userId = "user-1"))
        dao.updateDiscoveryPhase("""{"active":true}""")

        val result = dao.getUserProfile()
        assertEquals("""{"active":true}""", result?.discoveryPhaseJson)
    }

    @Test
    fun updateDiscoveryPhase_canClearToNull() = runTest {
        dao.insertOrUpdateProfile(buildProfile(userId = "user-1", discoveryPhaseJson = """{"active":true}"""))
        dao.updateDiscoveryPhase(null)

        val result = dao.getUserProfile()
        assertNull(result?.discoveryPhaseJson)
    }

    private fun buildProfile(
        userId: String,
        email: String? = null,
        discoveryPhaseJson: String? = null,
    ): UserProfileEntity =
        UserProfileEntity(
            rowId = 0,
            userId = userId,
            email = email,
            displayName = null,
            optimalCycleMinutes = 90,
            sleepLatencyMinutes = 15,
            preferredCycleCount = 6,
            reminderTime = "18:00",
            notificationsEnabled = true,
            smartWakeWindowEnabled = false,
            discoveryPhaseJson = discoveryPhaseJson,
        )
}
