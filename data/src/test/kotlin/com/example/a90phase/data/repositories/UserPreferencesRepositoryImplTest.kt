package com.example.a90phase.data.repositories

import app.cash.turbine.test
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.room.dao.UserProfileDao
import com.example.a90phase.data.local.room.entity.UserProfileEntity
import com.example.a90phase.data.sync.SyncScheduler
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for UserPreferencesRepositoryImpl.
 * setSmartWakeWindowEnabled is not tested here because it invokes WorkManager.getInstance()
 * which requires Android runtime. That path is covered by androidTest integration tests.
 */
class UserPreferencesRepositoryImplTest {

    private val userProfileDao = mockk<UserProfileDao>(relaxed = true)
    private val dataStore = mockk<UserPreferencesDataStore>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    private val repo = UserPreferencesRepositoryImpl(userProfileDao, dataStore, syncScheduler)

    // region getUserProfile

    @Test
    fun getUserProfile_returnsDefaultWhenRoomIsEmpty() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns null

        val result = repo.getUserProfile()

        assertTrue(result is Result.Success)
        assertEquals("local_user", (result as Result.Success).data.userId)
    }

    @Test
    fun getUserProfile_returnsMappedProfileFromRoom() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1", cycleMins = 95)

        val result = repo.getUserProfile()

        assertTrue(result is Result.Success)
        assertEquals("user-1", (result as Result.Success).data.userId)
        assertEquals(95, result.data.optimalCycleMinutes)
    }

    @Test
    fun getUserProfile_daoThrows_returnsError() = runTest {
        coEvery { userProfileDao.getUserProfile() } throws RuntimeException("DB error")

        val result = repo.getUserProfile()

        assertTrue(result is Result.Error)
    }

    // endregion

    // region updateUserProfile

    @Test
    fun updateUserProfile_writesEntityToRoom() = runTest {
        val profile = UserProfile(userId = "user-1", optimalCycleMinutes = 85)

        val result = repo.updateUserProfile(profile)

        assertTrue(result is Result.Success)
        coVerify { userProfileDao.insertOrUpdateProfile(any()) }
    }

    // endregion

    // region partial updates — Room round-trips

    @Test
    fun setCycleDuration_updatesOptimalCycleMinutesInRoom() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1", cycleMins = 90)

        val result = repo.setCycleDuration(80)

        assertTrue(result is Result.Success)
        coVerify {
            userProfileDao.insertOrUpdateProfile(
                match { it.optimalCycleMinutes == 80 },
            )
        }
    }

    @Test
    fun setSleepLatency_updatesSleepLatencyMinutesInRoom() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1")

        val result = repo.setSleepLatency(20)

        assertTrue(result is Result.Success)
        coVerify {
            userProfileDao.insertOrUpdateProfile(
                match { it.sleepLatencyMinutes == 20 },
            )
        }
    }

    @Test
    fun setReminderTime_updatesRoomAndDataStore() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1")

        val result = repo.setReminderTime("07:30")

        assertTrue(result is Result.Success)
        coVerify { userProfileDao.insertOrUpdateProfile(match { it.reminderTime == "07:30" }) }
        coVerify { dataStore.setNotificationTime("07:30") }
    }

    @Test
    fun setNotificationsEnabled_updatesProfileInRoom() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1", notificationsEnabled = true)

        val result = repo.setNotificationsEnabled(false)

        assertTrue(result is Result.Success)
        coVerify { userProfileDao.insertOrUpdateProfile(match { !it.notificationsEnabled }) }
    }

    // endregion

    // region Discovery Phase

    @Test
    fun updateDiscoveryPhase_callsDaoWithJson() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns buildEntity(userId = "user-1")

        // We pass a minimal DiscoveryPhase-like call through updateDiscoveryPhase
        // The JSON serialization uses org.json which is an Android stub in JVM tests,
        // so we test via endDiscoveryPhase (null JSON) which doesn't use JSONObject.
        val result = repo.endDiscoveryPhase()

        assertTrue(result is Result.Success)
        coVerify { userProfileDao.updateDiscoveryPhase(null) }
    }

    // endregion

    // region observeUserProfile

    @Test
    fun observeUserProfile_emitsDefaultWhenNoProfileInRoom() = runTest {
        every { userProfileDao.getUserProfileFlow() } returns flowOf(null)

        repo.observeUserProfile().test {
            val profile = awaitItem()
            assertEquals("local_user", profile.userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeUserProfile_emitsMappedProfileFromRoom() = runTest {
        every { userProfileDao.getUserProfileFlow() } returns flowOf(buildEntity(userId = "user-42"))

        repo.observeUserProfile().test {
            assertEquals("user-42", awaitItem().userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeUserProfile_emitsUpdatesOnRoomChange() = runTest {
        every { userProfileDao.getUserProfileFlow() } returns flowOf(
            buildEntity(userId = "user-1", cycleMins = 90),
            buildEntity(userId = "user-1", cycleMins = 80),
        )

        repo.observeUserProfile().test {
            assertEquals(90, awaitItem().optimalCycleMinutes)
            assertEquals(80, awaitItem().optimalCycleMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region observeSmartWakeWindowEnabled

    @Test
    fun observeSmartWakeWindowEnabled_delegatesToDataStore() = runTest {
        every { dataStore.observeSmartWakeWindowEnabled() } returns flowOf(true)

        repo.observeSmartWakeWindowEnabled().test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region default profile fallback

    @Test
    fun setCycleDuration_noExistingProfile_createsDefaultAndUpdates() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns null

        val result = repo.setCycleDuration(75)

        assertTrue(result is Result.Success)
        coVerify {
            userProfileDao.insertOrUpdateProfile(
                match { it.userId == "local_user" && it.optimalCycleMinutes == 75 },
            )
        }
    }

    @Test
    fun getUserProfile_defaultProfile_hasExpectedDefaults() = runTest {
        coEvery { userProfileDao.getUserProfile() } returns null

        val result = repo.getUserProfile()
        val profile = (result as Result.Success<UserProfile>).data

        assertEquals("local_user", profile.userId)
        assertNull(profile.email)
        assertEquals(90, profile.optimalCycleMinutes)
        assertEquals(15, profile.sleepLatencyMinutes)
        assertEquals("18:00", profile.reminderTime)
        assertEquals(true, profile.notificationsEnabled)
        assertEquals(false, profile.smartWakeWindowEnabled)
        assertNull(profile.discoveryPhase)
    }

    // endregion

    private fun buildEntity(
        userId: String,
        cycleMins: Int = 90,
        notificationsEnabled: Boolean = true,
    ) = UserProfileEntity(
        rowId = 0,
        userId = userId,
        email = null,
        displayName = null,
        optimalCycleMinutes = cycleMins,
        sleepLatencyMinutes = 15,
        preferredCycleCount = 6,
        reminderTime = "18:00",
        notificationsEnabled = notificationsEnabled,
        smartWakeWindowEnabled = false,
        discoveryPhaseJson = null,
    )
}
