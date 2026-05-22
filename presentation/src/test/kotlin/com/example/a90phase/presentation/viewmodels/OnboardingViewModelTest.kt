package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.OnboardingRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        onboardingRepo: FakeOnboardingRepository = FakeOnboardingRepository(),
        prefsRepo: FakeOnboardingPrefsRepository = FakeOnboardingPrefsRepository(),
        notificationScheduler: NotificationScheduler = NoOpNotificationScheduler(),
    ) = OnboardingViewModel(onboardingRepo, prefsRepo, notificationScheduler)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial currentScreen is 0`() {
        val vm = viewModel()
        assertEquals(0, vm.uiState.value.currentScreen)
    }

    @Test
    fun `initial wake time defaults to 7h00`() {
        val vm = viewModel()
        assertEquals(7, vm.uiState.value.wakeHour)
        assertEquals(0, vm.uiState.value.wakeMinute)
    }

    @Test
    fun `loads feature flags from onboarding repository`() = runTest {
        val state = UserOnboardingState(
            dailyCheckInEnabled = true,
            bedtimeReminderEnabled = true,
        )
        val vm = viewModel(onboardingRepo = FakeOnboardingRepository(state))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.dailyCheckInEnabled)
        assertTrue(vm.uiState.value.bedtimeReminderEnabled)
        assertFalse(vm.uiState.value.morningRatingEnabled)
    }

    // ── onNextScreen ──────────────────────────────────────────────────────────

    @Test
    fun `onNextScreen advances currentScreen`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onNextScreen()

        assertEquals(1, vm.uiState.value.currentScreen)
    }

    @Test
    fun `onNextScreen from last screen triggers onOnboardingComplete`() = runTest {
        val repo = FakeOnboardingRepository()
        val vm = viewModel(onboardingRepo = repo)
        testDispatcher.scheduler.advanceUntilIdle()

        repeat(7) { vm.onNextScreen() }
        vm.onNextScreen()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isOnboardingComplete)
        assertTrue(repo.markCompletedCalled)
    }

    // ── onWakeTimeSelected ────────────────────────────────────────────────────

    @Test
    fun `onWakeTimeSelected updates wakeHour and wakeMinute`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onWakeTimeSelected(8, 30)

        assertEquals(8, vm.uiState.value.wakeHour)
        assertEquals(30, vm.uiState.value.wakeMinute)
    }

    @Test
    fun `onWakeTimeSelected saves to UserPreferencesRepository`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onWakeTimeSelected(6, 15)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(6, prefs.savedWakeHour)
        assertEquals(15, prefs.savedWakeMinute)
    }

    // ── onFeatureEnabled ──────────────────────────────────────────────────────

    @Test
    fun `onFeatureEnabled DailyCheckIn saves to UserPreferencesRepository`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFeatureEnabled(OnboardingFeature.DailyCheckIn, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.dailyCheckInEnabled!!)
        assertTrue(vm.uiState.value.dailyCheckInEnabled)
    }

    @Test
    fun `onFeatureEnabled BedtimeReminder saves to UserPreferencesRepository`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFeatureEnabled(OnboardingFeature.BedtimeReminder, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.bedtimeReminderEnabled!!)
    }

    @Test
    fun `onFeatureEnabled MorningRating saves to UserPreferencesRepository`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFeatureEnabled(OnboardingFeature.MorningRating, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.morningRatingEnabled!!)
    }

    @Test
    fun `onFeatureEnabled SmartWake saves to UserPreferencesRepository`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFeatureEnabled(OnboardingFeature.SmartWake, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.smartWakeEnabled!!)
        assertTrue(vm.uiState.value.smartWakeEnabled)
    }

    @Test
    fun `onFeatureEnabled is idempotent when called with false`() = runTest {
        val prefs = FakeOnboardingPrefsRepository()
        val vm = viewModel(prefsRepo = prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFeatureEnabled(OnboardingFeature.DailyCheckIn, false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(prefs.dailyCheckInEnabled!!)
        assertFalse(vm.uiState.value.dailyCheckInEnabled)
    }

    // ── onOnboardingComplete ──────────────────────────────────────────────────

    @Test
    fun `onOnboardingComplete marks repository as completed`() = runTest {
        val repo = FakeOnboardingRepository()
        val vm = viewModel(onboardingRepo = repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onOnboardingComplete()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(repo.markCompletedCalled)
    }

    @Test
    fun `onOnboardingComplete sets isOnboardingComplete true`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onOnboardingComplete()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isOnboardingComplete)
    }

    // ── onShowWakeTimePicker ──────────────────────────────────────────────────

    @Test
    fun `onShowWakeTimePicker updates showWakeTimePicker`() {
        val vm = viewModel()

        vm.onShowWakeTimePicker(true)
        assertTrue(vm.uiState.value.showWakeTimePicker)

        vm.onShowWakeTimePicker(false)
        assertFalse(vm.uiState.value.showWakeTimePicker)
    }
}

// ── Test doubles ──────────────────────────────────────────────────────────────

private class FakeOnboardingRepository(
    private val initialState: UserOnboardingState = UserOnboardingState(),
) : OnboardingRepository {
    var markCompletedCalled = false
    private val stateFlow = MutableStateFlow(initialState)

    override fun getOnboardingState(): Flow<UserOnboardingState> = stateFlow

    override suspend fun saveOnboardingState(state: UserOnboardingState): Result<Unit> {
        stateFlow.value = state
        return Result.Success(Unit)
    }

    override suspend fun markOnboardingCompleted(): Result<Unit> {
        markCompletedCalled = true
        stateFlow.value = stateFlow.value.copy(isCompleted = true)
        return Result.Success(Unit)
    }
}

private class FakeOnboardingPrefsRepository : UserPreferencesRepository {
    var savedWakeHour: Int? = null
    var savedWakeMinute: Int? = null
    var dailyCheckInEnabled: Boolean? = null
    var bedtimeReminderEnabled: Boolean? = null
    var morningRatingEnabled: Boolean? = null
    var morningBedtimeLogEnabled: Boolean? = null
    var smartWakeEnabled: Boolean? = null

    override suspend fun getUserProfile(): Result<UserProfile> =
        Result.Success(UserProfile(userId = "test"))
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> {
        smartWakeEnabled = enabled
        return Result.Success(Unit)
    }
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> {
        dailyCheckInEnabled = enabled
        return Result.Success(Unit)
    }
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> {
        bedtimeReminderEnabled = enabled
        return Result.Success(Unit)
    }
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = Result.Success(Unit)
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> {
        morningRatingEnabled = enabled
        return Result.Success(Unit)
    }
    override fun observeMorningRatingEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> {
        morningBedtimeLogEnabled = enabled
        return Result.Success(Unit)
    }
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = flowOf(true)
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> {
        savedWakeHour = hour
        savedWakeMinute = minute
        return Result.Success(Unit)
    }
    override fun observeUserProfile(): Flow<UserProfile> = flowOf(UserProfile(userId = "test"))
}
