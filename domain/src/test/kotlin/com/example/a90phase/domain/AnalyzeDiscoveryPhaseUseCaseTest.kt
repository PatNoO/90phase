package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DailyRating
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.usecases.AnalyzeDiscoveryPhaseUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AnalyzeDiscoveryPhaseUseCaseTest {
    private val today = LocalDate.now()

    // ── Happy path: winning shift applied ─────────────────────────────────────

    @Test
    fun `applies LongerLatency params when it has the highest average rating`() =
        runTest {
            val phase =
                phaseWithRatings(
                    longerLatencyRatings = listOf(5, 5, 4, 5, 4, 5, 5),
                    longerCyclesRatings = listOf(3, 3, 3, 3, 3, 3, 3),
                    fewerCyclesRatings = listOf(2, 2, 2, 2, 2, 2, 2),
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            val result = AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            assertTrue(result is Result.Success)
            val updated = (result as Result.Success).data
            // LongerLatency → latency 30, cycle 90, count 6
            assertEquals(30, updated.sleepLatencyMinutes)
            assertEquals(90, updated.optimalCycleMinutes)
            assertEquals(6, updated.preferredCycleCount)
        }

    @Test
    fun `applies LongerCycles params when it has the highest average rating`() =
        runTest {
            val phase =
                phaseWithRatings(
                    longerLatencyRatings = listOf(2, 2, 2, 2, 2, 2, 2),
                    longerCyclesRatings = listOf(5, 5, 5, 5, 5, 5, 5),
                    fewerCyclesRatings = listOf(3, 3, 3, 3, 3, 3, 3),
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            val result = AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            val updated = (result as Result.Success).data
            // LongerCycles → cycle 105, latency 15, count 6
            assertEquals(105, updated.optimalCycleMinutes)
            assertEquals(15, updated.sleepLatencyMinutes)
            assertEquals(6, updated.preferredCycleCount)
        }

    @Test
    fun `applies FewerCycles params when it has the highest average rating`() =
        runTest {
            val phase =
                phaseWithRatings(
                    longerLatencyRatings = listOf(2, 2, 2, 2, 2, 2, 2),
                    longerCyclesRatings = listOf(3, 3, 3, 3, 3, 3, 3),
                    fewerCyclesRatings = listOf(5, 5, 5, 5, 5, 5, 5),
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            val result = AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            val updated = (result as Result.Success).data
            // FewerCycles → cycle 90, latency 15, count 5
            assertEquals(90, updated.optimalCycleMinutes)
            assertEquals(15, updated.sleepLatencyMinutes)
            assertEquals(5, updated.preferredCycleCount)
        }

    @Test
    fun `discovery phase is cleared from profile after analysis`() =
        runTest {
            val phase =
                phaseWithRatings(
                    longerLatencyRatings = listOf(5, 5, 5, 5, 5, 5, 5),
                    longerCyclesRatings = listOf(3, 3, 3, 3, 3, 3, 3),
                    fewerCyclesRatings = listOf(2, 2, 2, 2, 2, 2, 2),
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            val updatedProfile = (prefsRepo.getUserProfile() as Result.Success).data
            assertEquals(null, updatedProfile.discoveryPhase)
        }

    // ── Error: no phase ────────────────────────────────────────────────────────

    @Test
    fun `returns InsufficientData when no discovery phase on profile`() =
        runTest {
            val result = AnalyzeDiscoveryPhaseUseCase(FakePreferencesRepository())()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.InsufficientData)
        }

    // ── Error: not enough ratings ──────────────────────────────────────────────

    @Test
    fun `returns InsufficientData when fewer than 7 ratings`() =
        runTest {
            val phase =
                DiscoveryPhase(
                    isActive = true,
                    currentShift = ShiftType.LongerLatency,
                    startDate = today,
                    weeklyRatings =
                        listOf(
                            DailyRating(today, rating = 4, shiftType = ShiftType.LongerLatency),
                            DailyRating(today.plusDays(1), rating = 3, shiftType = ShiftType.LongerLatency),
                        ),
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            val result = AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.InsufficientData)
        }

    @Test
    fun `returns InsufficientData when all ratings are null`() =
        runTest {
            val ratings = (0..6).map { DailyRating(today.plusDays(it.toLong()), rating = null, shiftType = ShiftType.LongerLatency) }
            val phase =
                DiscoveryPhase(
                    isActive = true,
                    currentShift = ShiftType.LongerLatency,
                    startDate = today,
                    weeklyRatings = ratings,
                )
            val prefsRepo = FakePreferencesRepository(defaultProfile().copy(discoveryPhase = phase))

            val result = AnalyzeDiscoveryPhaseUseCase(prefsRepo)()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.InsufficientData)
        }

    // ── Error propagation ──────────────────────────────────────────────────────

    @Test
    fun `propagates profile load error`() =
        runTest {
            val result = AnalyzeDiscoveryPhaseUseCase(FailingPreferencesRepository())()

            assertTrue(result is Result.Error)
        }

    // ── helpers ────────────────────────────────────────────────────────────────

    private fun phaseWithRatings(
        longerLatencyRatings: List<Int>,
        longerCyclesRatings: List<Int>,
        fewerCyclesRatings: List<Int>,
    ): DiscoveryPhase {
        var day = 0L
        val ratings =
            buildList {
                longerLatencyRatings.forEach { r ->
                    add(DailyRating(today.plusDays(day++), rating = r, shiftType = ShiftType.LongerLatency))
                }
                longerCyclesRatings.forEach { r ->
                    add(DailyRating(today.plusDays(day++), rating = r, shiftType = ShiftType.LongerCycles))
                }
                fewerCyclesRatings.forEach { r ->
                    add(DailyRating(today.plusDays(day++), rating = r, shiftType = ShiftType.FewerCycles))
                }
            }
        return DiscoveryPhase(
            isActive = true,
            currentShift = ShiftType.FewerCycles,
            startDate = today,
            weeklyRatings = ratings,
        )
    }
}
