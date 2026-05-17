package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.StartDiscoveryPhaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StartDiscoveryPhaseUseCaseTest {
    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    fun `returns Success with active LongerLatency phase when preconditions met`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(7) { repo.saveSleepLog(buildSleepLog("id-$it")) }
            val useCase = StartDiscoveryPhaseUseCase(repo, FakePreferencesRepository())

            val result = useCase()

            assertTrue(result is Result.Success)
            val phase = (result as Result.Success).data
            assertTrue(phase.isActive)
            assertEquals(ShiftType.LongerLatency, phase.currentShift)
        }

    @Test
    fun `start date is today`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(7) { repo.saveSleepLog(buildSleepLog("id-$it")) }

            val result = StartDiscoveryPhaseUseCase(repo, FakePreferencesRepository())()

            val phase = (result as Result.Success).data
            assertEquals(LocalDate.now(), phase.startDate)
        }

    @Test
    fun `succeeds with more than minimum logs`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(15) { repo.saveSleepLog(buildSleepLog("id-$it")) }

            val result = StartDiscoveryPhaseUseCase(repo, FakePreferencesRepository())()

            assertTrue(result is Result.Success)
        }

    // ── Precondition: insufficient logs ───────────────────────────────────────

    @Test
    fun `returns InsufficientData when fewer than 7 logs exist`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(6) { repo.saveSleepLog(buildSleepLog("id-$it")) }

            val result = StartDiscoveryPhaseUseCase(repo, FakePreferencesRepository())()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.InsufficientData)
        }

    @Test
    fun `returns InsufficientData when no logs exist`() =
        runTest {
            val result = StartDiscoveryPhaseUseCase(FakeSleepRepository(), FakePreferencesRepository())()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.InsufficientData)
        }

    // ── Precondition: already active ──────────────────────────────────────────

    @Test
    fun `returns DiscoveryPhaseAlreadyActive when phase is already running`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(7) { repo.saveSleepLog(buildSleepLog("id-$it")) }
            val activePhase =
                DiscoveryPhase(
                    isActive = true,
                    currentShift = ShiftType.LongerLatency,
                    startDate = LocalDate.now().minusDays(7),
                )
            val prefsRepo =
                FakePreferencesRepository(
                    profile = defaultProfile().copy(discoveryPhase = activePhase),
                )

            val result = StartDiscoveryPhaseUseCase(repo, prefsRepo)()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.DiscoveryPhaseAlreadyActive)
        }

    // ── Error propagation ──────────────────────────────────────────────────────

    @Test
    fun `propagates profile load error`() =
        runTest {
            val repo = FakeSleepRepository()
            repeat(7) { repo.saveSleepLog(buildSleepLog("id-$it")) }

            val result = StartDiscoveryPhaseUseCase(repo, FailingPreferencesRepository())()

            assertTrue(result is Result.Error)
        }
}

// ── Test doubles ───────────────────────────────────────────────────────────────

internal fun buildSleepLog(id: String) =
    com.example.a90phase.domain.entities.SleepLog(
        id = id,
        date = java.time.LocalDate.of(2024, 1, 15),
        wakeTime = java.time.Instant.parse("2024-01-15T07:00:00Z"),
        cycleCount = 5,
        cycleDurationUsed = 90,
        sleepLatencyUsed = 15,
    )

internal fun defaultProfile() = UserProfile(userId = "test")

internal class FakePreferencesRepository(
    private var profile: UserProfile = defaultProfile(),
) : UserPreferencesRepository {
    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(profile)

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        this.profile = profile
        return Result.Success(Unit)
    }

    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)

    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)

    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = Result.Success(Unit)

    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> {
        profile = profile.copy(discoveryPhase = phase)
        return Result.Success(Unit)
    }

    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> {
        profile = profile.copy(discoveryPhase = phase)
        return Result.Success(Unit)
    }

    override suspend fun endDiscoveryPhase(): Result<Unit> {
        profile = profile.copy(discoveryPhase = null)
        return Result.Success(Unit)
    }

    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}

internal class FailingPreferencesRepository : UserPreferencesRepository {
    private val error = Result.Error(DomainError.DatabaseError("DB unavailable"))

    override suspend fun getUserProfile(): Result<UserProfile> = error

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = error

    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = error

    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = error

    override suspend fun setReminderTime(time: String): Result<Unit> = error

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = error

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = error

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = error

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = error

    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setSelectedBedtime(
        hour: Int,
        minute: Int,
        cycleCount: Int,
        durationMinutes: Int,
    ): Result<Unit> = error

    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = error

    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = error

    override suspend fun endDiscoveryPhase(): Result<Unit> = error

    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}
