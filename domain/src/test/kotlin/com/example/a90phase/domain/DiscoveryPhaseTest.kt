package com.example.a90phase.domain

import com.example.a90phase.domain.entities.DailyRating
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.LocalDate

class DiscoveryPhaseTest {
    private val today = LocalDate.now()

    private fun phase(ratings: List<DailyRating> = emptyList()) =
        DiscoveryPhase(
            isActive = true,
            currentShift = ShiftType.LongerLatency,
            startDate = today,
            weeklyRatings = ratings,
        )

    // ── endDate ────────────────────────────────────────────────────────────────

    @Test
    fun `endDate is startDate plus 21 days`() {
        phase().endDate shouldBe today.plusDays(21)
    }

    // ── getDaysRemaining ───────────────────────────────────────────────────────

    @Test
    fun `getDaysRemaining returns positive value when within window`() {
        val p =
            DiscoveryPhase(
                isActive = true,
                currentShift = ShiftType.LongerLatency,
                startDate = today.minusDays(5),
            )
        val remaining = p.getDaysRemaining()
        (remaining > 0) shouldBe true
    }

    @Test
    fun `getDaysRemaining returns zero when past end date`() {
        val p =
            DiscoveryPhase(
                isActive = false,
                currentShift = ShiftType.FewerCycles,
                startDate = today.minusDays(30),
            )
        p.getDaysRemaining() shouldBe 0
    }

    // ── hasEnoughData ──────────────────────────────────────────────────────────

    @Test
    fun `hasEnoughData returns false when fewer than 7 ratings`() {
        val ratings = (0..5).map { DailyRating(today.plusDays(it.toLong()), rating = 4, shiftType = ShiftType.LongerLatency) }
        phase(ratings).hasEnoughData() shouldBe false
    }

    @Test
    fun `hasEnoughData returns true when exactly 7 ratings`() {
        val ratings = (0..6).map { DailyRating(today.plusDays(it.toLong()), rating = 4, shiftType = ShiftType.LongerLatency) }
        phase(ratings).hasEnoughData() shouldBe true
    }

    @Test
    fun `hasEnoughData returns true when more than 7 ratings`() {
        val ratings = (0..20).map { DailyRating(today.plusDays(it.toLong()), rating = 3, shiftType = ShiftType.LongerCycles) }
        phase(ratings).hasEnoughData() shouldBe true
    }

    @Test
    fun `hasEnoughData returns false when no ratings`() {
        phase().hasEnoughData() shouldBe false
    }

    // ── getAverageRating ───────────────────────────────────────────────────────

    @Test
    fun `getAverageRating returns null when no ratings`() {
        phase().getAverageRating().shouldBeNull()
    }

    @Test
    fun `getAverageRating returns null when all ratings are null`() {
        val ratings =
            listOf(
                DailyRating(today, rating = null, shiftType = ShiftType.LongerLatency),
                DailyRating(today.plusDays(1), rating = null, shiftType = ShiftType.LongerLatency),
            )
        phase(ratings).getAverageRating().shouldBeNull()
    }

    @Test
    fun `getAverageRating computes average of non-null ratings`() {
        val ratings =
            listOf(
                DailyRating(today, rating = 4, shiftType = ShiftType.LongerLatency),
                DailyRating(today.plusDays(1), rating = 2, shiftType = ShiftType.LongerLatency),
                DailyRating(today.plusDays(2), rating = null, shiftType = ShiftType.LongerLatency),
            )
        phase(ratings).getAverageRating().shouldNotBeNull() shouldBe (3.0 plusOrMinus 0.001)
    }

    // ── ShiftType computed properties ──────────────────────────────────────────

    @Test
    fun `LongerLatency getCycleDuration returns 90`() {
        ShiftType.LongerLatency.getCycleDuration() shouldBe 90
    }

    @Test
    fun `LongerLatency getSleepLatency returns 30`() {
        ShiftType.LongerLatency.getSleepLatency() shouldBe 30
    }

    @Test
    fun `LongerLatency getCycleCount returns 6`() {
        ShiftType.LongerLatency.getCycleCount() shouldBe 6
    }

    @Test
    fun `LongerCycles getCycleDuration returns 105`() {
        ShiftType.LongerCycles.getCycleDuration() shouldBe 105
    }

    @Test
    fun `LongerCycles getSleepLatency returns 15`() {
        ShiftType.LongerCycles.getSleepLatency() shouldBe 15
    }

    @Test
    fun `LongerCycles getCycleCount returns 6`() {
        ShiftType.LongerCycles.getCycleCount() shouldBe 6
    }

    @Test
    fun `FewerCycles getCycleDuration returns 90`() {
        ShiftType.FewerCycles.getCycleDuration() shouldBe 90
    }

    @Test
    fun `FewerCycles getSleepLatency returns 15`() {
        ShiftType.FewerCycles.getSleepLatency() shouldBe 15
    }

    @Test
    fun `FewerCycles getCycleCount returns 5`() {
        ShiftType.FewerCycles.getCycleCount() shouldBe 5
    }
}
