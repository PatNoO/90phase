package com.example.a90phase.presentation.viewmodels

import app.cash.turbine.test
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import com.example.a90phase.domain.repositories.SleepRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

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
        repo: SleepRepository = FakeSleepRepository(),
        insightsRepo: PatternInsightsRepository = FakeHistoryPatternInsightsRepository(),
    ) = HistoryViewModel(repo, insightsRepo)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel(FakeSleepRepository(MutableStateFlow(emptyList())))
        assertTrue(vm.uiState.value is HistoryUiState.Loading)
    }

    // ── Empty state ───────────────────────────────────────────────────────────

    @Test
    fun `emits Empty when repository returns no logs`() = runTest {
        val vm = viewModel(FakeSleepRepository(flowOf(emptyList())))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is HistoryUiState.Empty)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── Content state ─────────────────────────────────────────────────────────

    @Test
    fun `emits Content with correct total logs count`() = runTest {
        val logs = buildLogs(5)
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(5, state.totalLogs)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `calculates average rating correctly`() = runTest {
        val logs = listOf(
            buildLog(id = "1", rating = 4),
            buildLog(id = "2", rating = 2),
        )
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(3.0f, state.averageRating)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `average rating is 0 when no logs have ratings`() = runTest {
        val logs = listOf(buildLog(id = "1", rating = null))
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(0f, state.averageRating)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `identifies best day as day with highest rating`() = runTest {
        val base = LocalDate.of(2025, 5, 12) // Monday
        val logs = listOf(
            buildLog(id = "1", date = base, rating = 5),               // Monday
            buildLog(id = "2", date = base.plusDays(1), rating = 2),   // Tuesday
            buildLog(id = "3", date = base.plusDays(2), rating = 3),   // Wednesday
        )
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(DayOfWeek.MONDAY, state.bestDay)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `bestDay is null when all logs have no rating`() = runTest {
        val logs = listOf(buildLog(id = "1", rating = null))
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertNull(state.bestDay)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `logs in Content match exactly what repository emits`() = runTest {
        val logs = buildLogs(3)
        val vm = viewModel(FakeSleepRepository(flowOf(logs)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(logs, state.logs)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── Reactive updates ──────────────────────────────────────────────────────

    @Test
    fun `transitions from Empty to Content when logs are added`() = runTest {
        val flow = MutableStateFlow<List<SleepLog>>(emptyList())
        val vm = viewModel(FakeSleepRepository(flow))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is HistoryUiState.Empty)

            flow.value = buildLogs(2)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is HistoryUiState.Content)
            cancelAndConsumeRemainingEvents()
        }
    }
}

// ── Test doubles ──────────────────────────────────────────────────────────────

private class FakeHistoryPatternInsightsRepository : PatternInsightsRepository {
    override fun observePatternInsightsEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun setPatternInsightsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)
    override fun observeDismissedInsightIds(): Flow<Set<String>> = flowOf(emptySet())
    override suspend fun dismissInsight(id: String): Result<Unit> = Result.Success(Unit)
}

private class FakeSleepRepository(
    private val logsFlow: Flow<List<SleepLog>> = flowOf(emptyList()),
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

private fun buildLog(
    id: String,
    date: LocalDate = LocalDate.of(2025, 5, 15),
    rating: Int? = 4,
): SleepLog = SleepLog(
    id = id,
    date = date,
    wakeTime = Instant.parse("2025-05-15T06:30:00Z"),
    qualityRating = rating,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)

private fun buildLogs(count: Int): List<SleepLog> =
    (1..count).map { buildLog(id = it.toString()) }
