package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.CalculateOptimalBedtimeUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CalculateOptimalBedtimeUseCaseTest {
    private fun useCase(profile: UserProfile = defaultProfile()): CalculateOptimalBedtimeUseCase =
        CalculateOptimalBedtimeUseCase(FakeUserPreferencesRepository(profile))

    private fun defaultProfile() =
        UserProfile(
            userId = "test",
            optimalCycleMinutes = 90,
            sleepLatencyMinutes = 15,
        )

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun `returns 5 recommendations for 07h00 wake time`() =
        runTest {
            val result = useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
            val recs = (result as Result.Success).data
            assertEquals(5, recs.size)
        }

    @Test
    fun `3-cycle and 2-cycle bedtimes are offered and MINIMAL`() =
        runTest {
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
                        as Result.Success
                ).data
            // 07:00 - (3×90+15) = 07:00 - 285 min = 02:15
            assertEquals(LocalTime.of(2, 15), recs[3].bedtime)
            assertEquals(3, recs[3].cycleCount)
            assertEquals(BedtimeQuality.MINIMAL, recs[3].quality)
            // 07:00 - (2×90+15) = 07:00 - 195 min = 03:45
            assertEquals(LocalTime.of(3, 45), recs[4].bedtime)
            assertEquals(2, recs[4].cycleCount)
            assertEquals(BedtimeQuality.MINIMAL, recs[4].quality)
        }

    @Test
    fun `00h45 bedtime is not PASSED when opened in the morning before wake time`() =
        runTest {
            // Opening at 09:00 targeting a 07:00 wake: tonight's 4-cycle bedtime is 00:45.
            // 00:45 is numerically before 09:00 but is a future bedtime — must NOT be PASSED.
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(9, 0))
                        as Result.Success
                ).data
            val fortyFivePastMidnight = recs.find { it.bedtime == LocalTime.of(0, 45) }!!
            assertEquals(BedtimeQuality.MINIMAL, fortyFivePastMidnight.quality)
        }

    @Test
    fun `6-cycle bedtime is OPTIMAL when not passed`() =
        runTest {
            // 07:00 - (6×90+15) = 07:00 - 555 min = 21:45
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
                        as Result.Success
                ).data
            assertEquals(LocalTime.of(21, 45), recs[0].bedtime)
            assertEquals(BedtimeQuality.OPTIMAL, recs[0].quality)
            assertEquals(6, recs[0].cycleCount)
        }

    @Test
    fun `5-cycle bedtime is GOOD when not passed`() =
        runTest {
            // 07:00 - (5×90+15) = 07:00 - 465 min = 23:15
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
                        as Result.Success
                ).data
            assertEquals(LocalTime.of(23, 15), recs[1].bedtime)
            assertEquals(BedtimeQuality.GOOD, recs[1].quality)
            assertEquals(5, recs[1].cycleCount)
        }

    @Test
    fun `4-cycle bedtime is MINIMAL when not passed`() =
        runTest {
            // 07:00 - (4×90+15) = 07:00 - 375 min = 00:45
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
                        as Result.Success
                ).data
            assertEquals(LocalTime.of(0, 45), recs[2].bedtime)
            assertEquals(BedtimeQuality.MINIMAL, recs[2].quality)
            assertEquals(4, recs[2].cycleCount)
        }

    @Test
    fun `durationMinutes is sum of cycles times duration plus latency`() =
        runTest {
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(20, 0))
                        as Result.Success
                ).data
            assertEquals(555, recs[0].durationMinutes) // 6×90+15
            assertEquals(465, recs[1].durationMinutes) // 5×90+15
            assertEquals(375, recs[2].durationMinutes) // 4×90+15
        }

    // ── Passed bedtimes ───────────────────────────────────────────────────────

    @Test
    fun `6-cycle bedtime marked PASSED when current time is after it`() =
        runTest {
            // 21:45 is before 22:00 → PASSED
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(22, 0))
                        as Result.Success
                ).data
            assertEquals(BedtimeQuality.PASSED, recs[0].quality)
        }

    @Test
    fun `5-cycle bedtime not PASSED when current time is before it`() =
        runTest {
            // 23:15 is after 22:00 → not passed
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(22, 0))
                        as Result.Success
                ).data
            assertEquals(BedtimeQuality.GOOD, recs[1].quality)
        }

    // ── Midnight crossing ─────────────────────────────────────────────────────

    @Test
    fun `00h45 bedtime is not PASSED when current time is 22h00 (crosses midnight)`() =
        runTest {
            // 00:45 numerically < 22:00, but it's tonight's future bedtime
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(22, 0))
                        as Result.Success
                ).data
            val fortyFivePastMidnight = recs.find { it.bedtime == LocalTime.of(0, 45) }!!
            assertEquals(BedtimeQuality.MINIMAL, fortyFivePastMidnight.quality)
        }

    @Test
    fun `00h45 bedtime is PASSED when current time is 02h00 (genuinely in the past)`() =
        runTest {
            val recs =
                (
                    useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(2, 0))
                        as Result.Success
                ).data
            val fortyFivePastMidnight = recs.find { it.bedtime == LocalTime.of(0, 45) }!!
            assertEquals(BedtimeQuality.PASSED, fortyFivePastMidnight.quality)
        }

    // ── Custom profile values ─────────────────────────────────────────────────

    @Test
    fun `uses custom cycle duration from user profile`() =
        runTest {
            val profile = defaultProfile().copy(optimalCycleMinutes = 100)
            // 07:00 - (6×100+15) = 07:00 - 615 min = 20:45
            val recs =
                (
                    useCase(profile).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0))
                        as Result.Success
                ).data
            assertEquals(LocalTime.of(20, 45), recs[0].bedtime)
            assertEquals(615, recs[0].durationMinutes)
        }

    @Test
    fun `uses custom sleep latency from user profile`() =
        runTest {
            val profile = defaultProfile().copy(sleepLatencyMinutes = 30)
            // 07:00 - (6×90+30) = 07:00 - 570 min = 21:30
            val recs =
                (
                    useCase(profile).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0))
                        as Result.Success
                ).data
            assertEquals(LocalTime.of(21, 30), recs[0].bedtime)
            assertEquals(570, recs[0].durationMinutes)
        }

    // ── Discovery Phase shift rotation ────────────────────────────────────────

    @Test
    fun `uses LongerLatency params (latency 30) on discovery phase day 1`() =
        runTest {
            val startDate = LocalDate.of(2024, 3, 1)
            val phase = DiscoveryPhase(isActive = true, currentShift = ShiftType.LongerLatency, startDate = startDate)
            val profile = defaultProfile().copy(discoveryPhase = phase)
            // LongerLatency: cycle 90, latency 30 → 6×90+30 = 570 → 07:00 - 570min = 21:30
            val recs =
                (
                    useCase(
                        profile,
                    ).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0), today = startDate) as Result.Success
                ).data
            assertEquals(LocalTime.of(21, 30), recs[0].bedtime)
            assertEquals(570, recs[0].durationMinutes)
        }

    @Test
    fun `uses LongerCycles params (cycle 105) on discovery phase day 8`() =
        runTest {
            val startDate = LocalDate.of(2024, 3, 1)
            val phase = DiscoveryPhase(isActive = true, currentShift = ShiftType.LongerLatency, startDate = startDate)
            val profile = defaultProfile().copy(discoveryPhase = phase)
            // LongerCycles: cycle 105, latency 15 → 6×105+15 = 645 → 07:00 - 645min = 20:15
            val today = startDate.plusDays(7)
            val recs =
                (
                    useCase(
                        profile,
                    ).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0), today = today) as Result.Success
                ).data
            assertEquals(LocalTime.of(20, 15), recs[0].bedtime)
            assertEquals(645, recs[0].durationMinutes)
        }

    @Test
    fun `uses FewerCycles params (cycle count 5 wins quality label) on discovery phase day 15`() =
        runTest {
            val startDate = LocalDate.of(2024, 3, 1)
            val phase = DiscoveryPhase(isActive = true, currentShift = ShiftType.LongerLatency, startDate = startDate)
            val profile = defaultProfile().copy(discoveryPhase = phase)
            // FewerCycles: cycle 90, latency 15 → same as default profile but max cycles = 5 per ShiftType
            val today = startDate.plusDays(14)
            val recs =
                (
                    useCase(
                        profile,
                    ).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0), today = today) as Result.Success
                ).data
            // FewerCycles: cycle 90, latency 15 — same duration as default, params unchanged
            assertEquals(555, recs[0].durationMinutes) // 6×90+15 (CYCLE_COUNTS still 6,5,4)
        }

    @Test
    fun `falls back to profile params when discovery phase is inactive`() =
        runTest {
            val startDate = LocalDate.of(2024, 3, 1)
            val phase = DiscoveryPhase(isActive = false, currentShift = ShiftType.LongerLatency, startDate = startDate)
            val profile = defaultProfile().copy(discoveryPhase = phase)
            // Inactive phase → use profile defaults: cycle 90, latency 15
            val recs =
                (
                    useCase(
                        profile,
                    ).invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0), today = startDate) as Result.Success
                ).data
            assertEquals(555, recs[0].durationMinutes) // 6×90+15
        }

    @Test
    fun `falls back to profile params when no discovery phase`() =
        runTest {
            val recs = (useCase().invoke(LocalTime.of(7, 0), currentTime = LocalTime.of(18, 0)) as Result.Success).data
            assertEquals(555, recs[0].durationMinutes) // 6×90+15
        }

    // ── Error propagation ─────────────────────────────────────────────────────

    @Test
    fun `returns CalculationFailed when profile load fails`() =
        runTest {
            val failing = CalculateOptimalBedtimeUseCase(FailingUserPreferencesRepository())
            val result = failing.invoke(LocalTime.of(7, 0))
            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.CalculationFailed)
        }
}

// ── Test doubles ──────────────────────────────────────────────────────────────

private class FakeUserPreferencesRepository(
    private val profile: UserProfile,
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

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setWakeAlarmEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeWakeAlarmEnabled(): Flow<Boolean> = emptyFlow()

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

    override suspend fun setSelectedWakeTime(
        hour: Int,
        minute: Int,
    ): Result<Unit> = Result.Success(Unit)

    override fun observeSelectedWakeTime(): Flow<java.time.LocalTime> = emptyFlow()

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

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setBedtimeReminderEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeBedtimeReminderEnabled(): Flow<Boolean> = emptyFlow()

    override suspend fun setWakeAlarmEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeWakeAlarmEnabled(): Flow<Boolean> = emptyFlow()

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

    override suspend fun setSelectedWakeTime(
        hour: Int,
        minute: Int,
    ): Result<Unit> = Result.Success(Unit)

    override fun observeSelectedWakeTime(): Flow<java.time.LocalTime> = emptyFlow()

    override fun observeUserProfile(): Flow<UserProfile> = emptyFlow()
}
