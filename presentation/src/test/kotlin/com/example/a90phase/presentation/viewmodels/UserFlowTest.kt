package com.example.a90phase.presentation.viewmodels

import app.cash.turbine.test
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.repositories.OnboardingRepository
import com.example.a90phase.domain.repositories.SleepRepository
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class UserFlowTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Flow 1: onboarding → save wake time → Home shows recommendations ──────

    @Test
    fun `flow 1 onboarding wake time is saved and calculator shows recommendations for it`() = runTest {
        val sharedPrefs = CapturingFlowPrefsRepository()
        val onboardingVm = OnboardingViewModel(SimpleFlowOnboardingRepository(), sharedPrefs)
        val calculatorVm = CalculatorViewModel(sharedPrefs, NoOpAlarmRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        onboardingVm.onWakeTimeSelected(8, 30)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(8, sharedPrefs.savedWakeHour)
        assertEquals(30, sharedPrefs.savedWakeMinute)

        calculatorVm.onWakeTimeChanged(
            LocalTime.of(sharedPrefs.savedWakeHour!!, sharedPrefs.savedWakeMinute!!),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = calculatorVm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(LocalTime.of(8, 30), state.wakeTime)
        assertTrue(state.bedtimes.isNotEmpty())
    }

    // ── Flow 2: change wake time → new recommendations calculated ─────────────

    @Test
    fun `flow 2 changing wake time recalculates recommendations`() = runTest {
        val vm = CalculatorViewModel(CapturingFlowPrefsRepository(), NoOpAlarmRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        val initial = vm.uiState.value as SleepCalculatorUiState.Success
        val initialBedtime = initial.bedtimes.first().bedtime
        assertEquals(LocalTime.of(7, 0), initial.wakeTime)

        vm.onWakeTimeChanged(LocalTime.of(9, 0))
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(LocalTime.of(9, 0), updated.wakeTime)
        assertTrue(updated.bedtimes.isNotEmpty())
        assertNotEquals(initialBedtime, updated.bedtimes.first().bedtime)
    }

    // ── Flow 3: select bedtime → bedtime reminder scheduled ───────────────────

    @Test
    fun `flow 3 selecting bedtime persists it to repository for reminder scheduling`() = runTest {
        val prefs = CapturingFlowPrefsRepository()
        val vm = CalculatorViewModel(prefs, NoOpAlarmRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value as SleepCalculatorUiState.Success
        val selected = state.bedtimes[0]

        vm.onBedtimeSelected(selected, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(selected.bedtime.hour, prefs.savedBedtimeHour)
        assertEquals(selected.bedtime.minute, prefs.savedBedtimeMinute)
        assertEquals(selected.cycleCount, prefs.savedBedtimeCycleCount)
        val afterSelect = vm.uiState.value as SleepCalculatorUiState.Success
        assertEquals(0, afterSelect.selectedBedtimeIndex)
    }

    // ── Flow 4: rate sleep → log saved → History updates ──────────────────────

    @Test
    fun `flow 4 saving rated sleep log is reflected in history`() = runTest {
        val sleepRepo = MutableFlowSleepRepository()
        val historyVm = HistoryViewModel(sleepRepo)

        historyVm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is HistoryUiState.Empty)

            sleepRepo.saveSleepLog(buildFlowLog(id = "log-1", rating = 4))
            testDispatcher.scheduler.advanceUntilIdle()

            val content = awaitItem() as HistoryUiState.Content
            assertEquals(1, content.totalLogs)
            assertEquals(4f, content.averageRating)
            cancelAndConsumeRemainingEvents()
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun buildFlowLog(id: String, rating: Int? = null): SleepLog = SleepLog(
    id = id,
    date = LocalDate.of(2025, 5, 15),
    wakeTime = Instant.parse("2025-05-15T06:30:00Z"),
    qualityRating = rating,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)

private fun flowTestProfile() = UserProfile(
    userId = "flow-test",
    optimalCycleMinutes = 90,
    sleepLatencyMinutes = 15,
)

// ── Test doubles ──────────────────────────────────────────────────────────────

private class CapturingFlowPrefsRepository : UserPreferencesRepository {
    var savedWakeHour: Int? = null
    var savedWakeMinute: Int? = null
    var savedBedtimeHour: Int = -1
    var savedBedtimeMinute: Int = -1
    var savedBedtimeCycleCount: Int = -1

    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(flowTestProfile())
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)
    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDailyCheckInEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> {
        savedBedtimeHour = hour
        savedBedtimeMinute = minute
        savedBedtimeCycleCount = cycleCount
        return Result.Success(Unit)
    }
    override suspend fun setMorningRatingEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningRatingEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setMorningBedtimeLogEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeMorningBedtimeLogEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setFirebaseSyncEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeFirebaseSyncEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)
    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)
    override suspend fun setSelectedWakeTime(hour: Int, minute: Int): Result<Unit> {
        savedWakeHour = hour
        savedWakeMinute = minute
        return Result.Success(Unit)
    }
    override fun observeUserProfile(): Flow<UserProfile> = flowOf(flowTestProfile())
}

private class NoOpAlarmRepository : AlarmRepository {
    override suspend fun getNextAlarm(): Result<SystemAlarm?> = Result.Success(null)
    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> = Result.Success(emptyList())
}

private class SimpleFlowOnboardingRepository : OnboardingRepository {
    override fun getOnboardingState(): Flow<UserOnboardingState> = flowOf(UserOnboardingState())
    override suspend fun saveOnboardingState(state: UserOnboardingState): Result<Unit> = Result.Success(Unit)
    override suspend fun markOnboardingCompleted(): Result<Unit> = Result.Success(Unit)
}

private class MutableFlowSleepRepository : SleepRepository {
    private val _logsFlow = MutableStateFlow<List<SleepLog>>(emptyList())
    private val logs = mutableListOf<SleepLog>()

    override fun getAllSleepLogs(): Flow<List<SleepLog>> = _logsFlow
    override fun getSleepLog(id: String): Flow<SleepLog?> = flowOf(null)
    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = flowOf(emptyList())
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        logs.add(log)
        _logsFlow.value = logs.toList()
        return Result.Success(Unit)
    }
    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteSleepLog(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> = Result.Success(emptyList())
    override suspend fun getLastSyncTimestamp(): Result<Instant> = Result.Success(Instant.EPOCH)
    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = Result.Success(Unit)
}
