package com.example.a90phase.data.repositories

import app.cash.turbine.test
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.datastore.toJson
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserOnboardingState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** JVM unit tests for OnboardingRepositoryImpl. */
class OnboardingRepositoryImplTest {

    private val dataStore = mockk<UserPreferencesDataStore>(relaxed = true)
    private val repo = OnboardingRepositoryImpl(dataStore)

    // region getOnboardingState

    @Test
    fun getOnboardingState_nullJson_emitsDefaultState() = runTest {
        every { dataStore.observeOnboardingState() } returns flowOf(null)

        repo.getOnboardingState().test {
            val state = awaitItem()
            assertFalse(state.isCompleted)
            assertFalse(state.dailyCheckInEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getOnboardingState_validJson_emitsParsedState() = runTest {
        val stored = UserOnboardingState(isCompleted = true, dailyCheckInEnabled = true)
        every { dataStore.observeOnboardingState() } returns flowOf(stored.toJson())

        repo.getOnboardingState().test {
            val state = awaitItem()
            assertTrue(state.isCompleted)
            assertTrue(state.dailyCheckInEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getOnboardingState_emitsUpdatesOnDataStoreChange() = runTest {
        val first = UserOnboardingState(isCompleted = false)
        val second = UserOnboardingState(isCompleted = true)
        every { dataStore.observeOnboardingState() } returns flowOf(first.toJson(), second.toJson())

        repo.getOnboardingState().test {
            assertFalse(awaitItem().isCompleted)
            assertTrue(awaitItem().isCompleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region saveOnboardingState

    @Test
    fun saveOnboardingState_writesJsonAndCompletionFlag() = runTest {
        val state = UserOnboardingState(isCompleted = true, smartWakeWindowEnabled = true)

        val result = repo.saveOnboardingState(state)

        assertTrue(result is Result.Success)
        coVerify { dataStore.setOnboardingState(any()) }
        coVerify { dataStore.setOnboardingComplete(true) }
    }

    @Test
    fun saveOnboardingState_notCompleted_setsCompletionFlagFalse() = runTest {
        val state = UserOnboardingState(isCompleted = false)

        repo.saveOnboardingState(state)

        coVerify { dataStore.setOnboardingComplete(false) }
    }

    @Test
    fun saveOnboardingState_dataStoreThrows_returnsError() = runTest {
        coEvery { dataStore.setOnboardingState(any()) } throws RuntimeException("write failed")

        val result = repo.saveOnboardingState(UserOnboardingState())

        assertTrue(result is Result.Error)
    }

    // endregion

    // region markOnboardingCompleted

    @Test
    fun markOnboardingCompleted_noExistingState_setsIsCompletedTrue() = runTest {
        every { dataStore.observeOnboardingState() } returns flowOf(null)

        val result = repo.markOnboardingCompleted()

        assertTrue(result is Result.Success)
        coVerify { dataStore.setOnboardingState(any()) }
        coVerify { dataStore.setOnboardingComplete(true) }
    }

    @Test
    fun markOnboardingCompleted_existingState_preservesOtherFlags() = runTest {
        val existing = UserOnboardingState(dailyCheckInEnabled = true, smartWakeWindowEnabled = true)
        every { dataStore.observeOnboardingState() } returns flowOf(existing.toJson())

        repo.markOnboardingCompleted()

        coVerify {
            dataStore.setOnboardingState(
                match { json ->
                    json.contains("\"isCompleted\":true") &&
                        json.contains("\"dailyCheckInEnabled\":true") &&
                        json.contains("\"smartWakeWindowEnabled\":true")
                },
            )
        }
    }

    @Test
    fun markOnboardingCompleted_dataStoreThrows_returnsError() = runTest {
        every { dataStore.observeOnboardingState() } returns flowOf(null)
        coEvery { dataStore.setOnboardingState(any()) } throws RuntimeException("write failed")

        val result = repo.markOnboardingCompleted()

        assertTrue(result is Result.Error)
    }

    // endregion

    // region default state edge cases

    @Test
    fun getOnboardingState_corruptJson_emitsDefaultState() = runTest {
        every { dataStore.observeOnboardingState() } returns flowOf("corrupt{}")

        repo.getOnboardingState().test {
            val state = awaitItem()
            assertEquals(UserOnboardingState(), state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion
}
