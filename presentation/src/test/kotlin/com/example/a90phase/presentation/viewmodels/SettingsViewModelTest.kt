package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import app.cash.turbine.test
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

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
        prefsRepo: FakeSettingsPrefsRepository = FakeSettingsPrefsRepository(),
        sleepRepo: FakeSettingsSleepRepository = FakeSettingsSleepRepository(),
        insightsRepo: FakeSettingsPatternInsightsRepository = FakeSettingsPatternInsightsRepository(),
        notificationScheduler: NotificationScheduler = NoOpNotificationScheduler(),
    ) = SettingsViewModel(prefsRepo, sleepRepo, insightsRepo, notificationScheduler)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() {
        val vm = viewModel()
        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun `loads profile values into state`() = runTest {
        val prefs = FakeSettingsPrefsRepository(
            profile = defaultProfile().copy(
                optimalCycleMinutes = 100,
                sleepLatencyMinutes = 20,
                reminderTime = "19:30",
            ),
        )
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(100, state.cycleLengthMin)
        assertEquals(20, state.sleepLatencyMin)
        assertEquals(19, state.checkInHour)
        assertEquals(30, state.checkInMinute)
        assertTrue(!state.isLoading)
    }

    // ── Preference saves ──────────────────────────────────────────────────────

    @Test
    fun `onCycleDurationChanged saves to repository`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onCycleDurationChanged(105)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(105, prefs.savedCycleDuration)
    }

    @Test
    fun `onSleepLatencyChanged saves to repository`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onSleepLatencyChanged(25)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(25, prefs.savedSleepLatency)
    }

    @Test
    fun `onReminderTimeChanged saves formatted time to repository`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onReminderTimeChanged(hour = 9, minute = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("09:05", prefs.savedReminderTime)
    }

    @Test
    fun `onCycleDurationChanged emits error on failure`() = runTest {
        val prefs = FakeSettingsPrefsRepository(failSave = true)
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.errors.test {
            vm.onCycleDurationChanged(105)
            testDispatcher.scheduler.advanceUntilIdle()
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Firebase sync ─────────────────────────────────────────────────────────

    @Test
    fun `onFirebaseSyncToggled persists to repository`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onFirebaseSyncToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, prefs.savedFirebaseSync)
    }

    // ── Rating days count ─────────────────────────────────────────────────────

    @Test
    fun `ratingDaysCount reflects logs with quality ratings`() = runTest {
        val logs = listOf(
            buildLog("1", rating = 4),
            buildLog("2", rating = null),
            buildLog("3", rating = 5),
        )
        val sleep = FakeSettingsSleepRepository(MutableStateFlow(logs))
        val vm = viewModel(sleepRepo = sleep)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.ratingDaysCount)
    }

    // ── Discovery Phase ───────────────────────────────────────────────────────

    @Test
    fun `discoveryPhaseActive reflects active phase from profile`() = runTest {
        val phase = DiscoveryPhase(
            isActive = true,
            currentShift = ShiftType.LongerLatency,
            startDate = LocalDate.now().minusDays(2),
        )
        val prefs = FakeSettingsPrefsRepository(
            profile = defaultProfile().copy(discoveryPhase = phase),
        )
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.discoveryPhaseActive)
    }

    @Test
    fun `discoveryPhaseActive is false when no discovery phase`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!vm.uiState.value.discoveryPhaseActive)
    }

    @Test
    fun `onStartDiscoveryPhase sets discoveryStartError on insufficient data`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val sleep = FakeSettingsSleepRepository(flowOf(emptyList()))
        val vm = viewModel(prefs, sleep)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onStartDiscoveryPhase()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.uiState.value.discoveryStartError)
    }

    @Test
    fun `onCancelDiscoveryPhase calls endDiscoveryPhase on repository`() = runTest {
        val prefs = FakeSettingsPrefsRepository()
        val vm = viewModel(prefs)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onCancelDiscoveryPhase()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(prefs.endDiscoveryPhaseCalled)
    }
}

// ── Test doubles ──────────────────────────────────────────────────────────────

private fun defaultProfile() = UserProfile(
    userId = "test",
    optimalCycleMinutes = 90,
    sleepLatencyMinutes = 15,
    reminderTime = "18:00",
)

private class FakeSettingsPrefsRepository(
    private val profile: UserProfile = defaultProfile(),
    private val failSave: Boolean = false,
) : UserPreferencesRepository {

    var savedCycleDuration: Int? = null
    var savedSleepLatency: Int? = null
    var savedReminderTime: String? = null
    var savedFirebaseSync: Boolean? = null
    var endDiscoveryPhaseCalled = false

    private val profileFlow = MutableStateFlow(profile)

    private fun saveResult(): Result<Unit> = if (failSave) {
        Result.Error(DomainError.DatabaseError("save failed"))
    } else {
        Result.Success(Unit)
    }

    override fun observeUserProfile(): Flow<UserProfile> = profileFlow
    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(profile)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> {
        savedCycleDuration = minutes
        return saveResult()
    }
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> {
        savedSleepLatency = minutes
        return saveResult()
    }
    override suspend fun setReminderTime(time: String): Result<Unit> {
        savedReminderTime = time
        return saveResult()
    }
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> {
        savedFirebaseSync = enabled
        return Result.Success(Unit)
    }
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun endDiscoveryPhase(): Result<Unit> {
        endDiscoveryPhaseCalled = true
        return Result.Success(Unit)
    }
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setWakeAlarmEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeWakeAlarmEnabled(): Flow<Boolean> = MutableStateFlow(true)
    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = Result.Success(Unit)
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningRatingEnabled(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> {
        profileFlow.value = profileFlow.value.copy(discoveryPhase = phase)
        return Result.Success(Unit)
    }
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> = Result.Success(Unit)
    override fun observeSelectedWakeTime(): Flow<LocalTime> = MutableStateFlow(LocalTime.of(7, 0))
}

private class FakeSettingsPatternInsightsRepository : PatternInsightsRepository {
    override fun observePatternInsightsEnabled(): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setPatternInsightsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDismissedInsightIds(): Flow<Set<String>> = MutableStateFlow(emptySet())
    override suspend fun dismissInsight(id: String): Result<Unit> = Result.Success(Unit)
}

private class FakeSettingsSleepRepository(
    private val logsFlow: Flow<List<SleepLog>> = MutableStateFlow(emptyList()),
) : SleepRepository {
    override fun getAllSleepLogs(): Flow<List<SleepLog>> = logsFlow
    override fun getSleepLog(id: String): Flow<SleepLog?> = flowOf(null)
    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = flowOf(emptyList())
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteSleepLog(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> = Result.Success(emptyList())
    override suspend fun getLastSyncTimestamp(): Result<Instant> = Result.Success(Instant.EPOCH)
    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = Result.Success(Unit)
}

private fun buildLog(id: String, rating: Int?) = SleepLog(
    id = id,
    date = LocalDate.of(2025, 5, 15),
    wakeTime = Instant.parse("2025-05-15T06:30:00Z"),
    qualityRating = rating,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)
