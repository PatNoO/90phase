package com.example.a90phase.presentation.viewmodels

import app.cash.turbine.test
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

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
        userPrefsRepo: UserPreferencesRepository = FakeUserPreferencesRepository(),
        alarmRepo: AlarmRepository = FakeAlarmRepository(),
        notificationScheduler: NotificationScheduler = NoOpNotificationScheduler(),
        sleepRepo: SleepRepository = NoOpSleepRepository(),
    ) = CalculatorViewModel(userPrefsRepo, alarmRepo, notificationScheduler, sleepRepo)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading then transitions to Success`() = runTest {
        viewModel().uiState.test {
            val first = awaitItem()
            assertTrue("Expected Loading first", first is SleepCalculatorUiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val second = awaitItem()
            assertTrue("Expected Success", second is SleepCalculatorUiState.Success)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `initial wake time defaults to 07h00`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(LocalTime.of(7, 0), state.wakeTime)
    }

    @Test
    fun `initial state has 5 bedtime recommendations`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(5, state.bedtimes.size)
    }

    @Test
    fun `initial selected bedtime index is -1`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(-1, state.selectedBedtimeIndex)
    }

    // ── onWakeTimeChanged ────────────────────────────────────────────────────

    @Test
    fun `onWakeTimeChanged updates wake time in Success state`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onWakeTimeChanged(LocalTime.of(8, 30))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(LocalTime.of(8, 30), state.wakeTime)
    }

    @Test
    fun `onWakeTimeChanged transitions to Loading then Success`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // current Success
            vm.onWakeTimeChanged(LocalTime.of(9, 0))
            assertTrue(awaitItem() is SleepCalculatorUiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is SleepCalculatorUiState.Success)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onWakeTimeChanged emits Error when profile load fails`() = runTest {
        val vm = viewModel(userPrefsRepo = FailingUserPreferencesRepository())
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is SleepCalculatorUiState.Error)
    }

    // ── onBedtimeSelected ────────────────────────────────────────────────────

    @Test
    fun `onBedtimeSelected updates selectedBedtimeIndex`() = runTest {
        val vm = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        val bedtime = state.bedtimes[1]
        vm.onBedtimeSelected(bedtime, 1)

        val updated = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(1, updated.selectedBedtimeIndex)
    }

    @Test
    fun `onBedtimeSelected persists selection to repository`() = runTest {
        val repo = CapturingUserPreferencesRepository()
        val vm = viewModel(userPrefsRepo = repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        val bedtime = state.bedtimes[0]
        vm.onBedtimeSelected(bedtime, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(bedtime.bedtime.hour, repo.savedHour)
        assertEquals(bedtime.bedtime.minute, repo.savedMinute)
        assertEquals(bedtime.cycleCount, repo.savedCycleCount)
        assertEquals(bedtime.durationMinutes, repo.savedDurationMinutes)
    }

    @Test
    fun `onBedtimeSelected does nothing when state is not Success`() = runTest {
        val vm = viewModel()
        val fakeBedtime = BedtimeRecommendation(
            bedtime = LocalTime.of(22, 0),
            cycleCount = 6,
            quality = BedtimeQuality.OPTIMAL,
            durationMinutes = 450,
        )
        // Call before the init coroutine resolves — state is Loading
        vm.onBedtimeSelected(fakeBedtime, 0)
        assertTrue(vm.uiState.value is SleepCalculatorUiState.Loading)
    }

    // ── System alarm ─────────────────────────────────────────────────────────

    @Test
    fun `nextSystemAlarm is populated when alarm repo returns data`() = runTest {
        val alarm = SystemAlarm(
            time = Instant.parse("2024-03-01T06:30:00Z"),
            label = "Morning",
        )
        val vm = viewModel(alarmRepo = FakeAlarmRepository(listOf(alarm)))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(alarm, state.nextSystemAlarm)
    }

    @Test
    fun `nextSystemAlarm is null when alarm repo returns empty`() = runTest {
        val vm = viewModel(alarmRepo = FakeAlarmRepository(emptyList()))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(null, state.nextSystemAlarm)
    }
}

// ── Test doubles ──────────────────────────────────────────────────────────────

private class FakeAlarmRepository(
    private val alarms: List<SystemAlarm> = emptyList(),
) : AlarmRepository {
    override suspend fun getNextAlarm(): Result<SystemAlarm?> = Result.Success(alarms.firstOrNull())
    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> = Result.Success(alarms)
    override suspend fun setAlarm(wakeTime: LocalTime): Result<Unit> = Result.Success(Unit)
    override suspend fun dismissAlarm(): Result<Unit> = Result.Success(Unit)
}

private class NoOpSleepRepository : SleepRepository {
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override fun getAllSleepLogs(): Flow<List<SleepLog>> = emptyFlow()
    override fun getSleepLog(id: String): Flow<SleepLog?> = emptyFlow()
    override fun getSleepLogsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepLog>> = emptyFlow()
    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteSleepLog(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> = Result.Success(emptyList())
    override suspend fun getLastSyncTimestamp(): Result<Instant> = Result.Success(Instant.EPOCH)
    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = Result.Success(Unit)
}

private class FakeUserPreferencesRepository(
    private val profile: UserProfile = UserProfile(
        userId = "test",
        optimalCycleMinutes = 90,
        sleepLatencyMinutes = 15,
    ),
) : UserPreferencesRepository {
    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(profile)
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = Result.Success(Unit)
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningRatingEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> = Result.Success(Unit)
    override fun observeSelectedWakeTime(): Flow<LocalTime> = emptyFlow()
    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}

private class FailingUserPreferencesRepository : UserPreferencesRepository {
    override suspend fun getUserProfile(): Result<UserProfile> = Result.Error(DomainError.DatabaseError("DB unavailable"))
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = Result.Success(Unit)
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningRatingEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> = Result.Success(Unit)
    override fun observeSelectedWakeTime(): Flow<LocalTime> = emptyFlow()
    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}

private class CapturingUserPreferencesRepository : UserPreferencesRepository {
    var savedHour = -1
    var savedMinute = -1
    var savedCycleCount = -1
    var savedDurationMinutes = -1

    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(
        UserProfile(userId = "test", optimalCycleMinutes = 90, sleepLatencyMinutes = 15),
    )
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(true)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
    override suspend fun setSelectedBedtime(hour: Int, minute: Int, cycleCount: Int, durationMinutes: Int): Result<Unit> {
        savedHour = hour
        savedMinute = minute
        savedCycleCount = cycleCount
        savedDurationMinutes = durationMinutes
        return Result.Success(Unit)
    }
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningRatingEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = emptyFlow()
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> = Result.Success(Unit)
    override fun observeSelectedWakeTime(): Flow<LocalTime> = emptyFlow()
    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}
