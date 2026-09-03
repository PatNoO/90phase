package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.presentation.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

@OptIn(ExperimentalCoroutinesApi::class)
class LogDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: SleepRepository, logId: String = "1") = LogDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Route.LogDetail.ARG_LOG_ID to logId)),
        sleepRepository = repo,
    )

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel(FakeLogDetailSleepRepository(flowOf(PreviewLog)))
        assertTrue(vm.uiState.value is LogDetailUiState.Loading)
    }

    @Test
    fun `emits Content when log is found`() = runTest {
        val vm = viewModel(FakeLogDetailSleepRepository(flowOf(PreviewLog)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as LogDetailUiState.Content
            assertEquals(PreviewLog, state.log)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits NotFound when log is null`() = runTest {
        val vm = viewModel(FakeLogDetailSleepRepository(flowOf(null)))
        vm.uiState.test {
            awaitItem() // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is LogDetailUiState.NotFound)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits Deleted event when delete succeeds`() = runTest {
        val vm = viewModel(FakeLogDetailSleepRepository(flowOf(PreviewLog), deleteResult = Result.Success(Unit)))
        vm.events.test {
            vm.onDeleteConfirmed()
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is LogDetailEvent.Deleted)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits DeleteFailed event when delete fails`() = runTest {
        val error = com.example.a90phase.domain.common.DomainError.DatabaseError("boom")
        val vm = viewModel(FakeLogDetailSleepRepository(flowOf(PreviewLog), deleteResult = Result.Error(error)))
        vm.events.test {
            vm.onDeleteConfirmed()
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is LogDetailEvent.DeleteFailed)
            cancelAndConsumeRemainingEvents()
        }
    }
}

private val PreviewLog = SleepLog(
    id = "1",
    date = LocalDate.of(2025, 5, 15),
    wakeTime = Instant.parse("2025-05-15T06:30:00Z"),
    qualityRating = 4,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)

private class FakeLogDetailSleepRepository(
    private val logFlow: Flow<SleepLog?> = flowOf(null),
    private val deleteResult: Result<Unit> = Result.Success(Unit),
) : SleepRepository {
    override fun getAllSleepLogs(): Flow<List<SleepLog>> = flowOf(emptyList())
    override fun getSleepLog(id: String): Flow<SleepLog?> = logFlow
    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = flowOf(emptyList())
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> = Result.Success(Unit)
    override suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit> = Result.Success(Unit)
    override suspend fun deleteSleepLog(id: String): Result<Unit> = deleteResult
    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> = Result.Success(emptyList())
    override suspend fun getLastSyncTimestamp(): Result<Instant> = Result.Success(Instant.EPOCH)
    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> = Result.Success(Unit)
}
