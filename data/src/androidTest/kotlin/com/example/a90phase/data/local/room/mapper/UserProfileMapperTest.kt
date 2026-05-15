package com.example.a90phase.data.local.room.mapper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.a90phase.domain.entities.DailyRating
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserProfileMapperTest {

    private val baseDate = LocalDate.of(2026, 5, 15)

    @Test
    fun userProfile_roundTrip_withoutDiscoveryPhase() {
        val original = UserProfile(
            userId = "user-1",
            email = "test@example.com",
            displayName = "Test User",
            optimalCycleMinutes = 90,
            sleepLatencyMinutes = 15,
            preferredCycleCount = 6,
            reminderTime = "18:00",
            notificationsEnabled = true,
            smartWakeWindowEnabled = false,
            discoveryPhase = null,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original.userId, roundTripped.userId)
        assertEquals(original.email, roundTripped.email)
        assertEquals(original.displayName, roundTripped.displayName)
        assertEquals(original.optimalCycleMinutes, roundTripped.optimalCycleMinutes)
        assertEquals(original.sleepLatencyMinutes, roundTripped.sleepLatencyMinutes)
        assertEquals(original.preferredCycleCount, roundTripped.preferredCycleCount)
        assertEquals(original.reminderTime, roundTripped.reminderTime)
        assertEquals(original.notificationsEnabled, roundTripped.notificationsEnabled)
        assertEquals(original.smartWakeWindowEnabled, roundTripped.smartWakeWindowEnabled)
        assertNull(roundTripped.discoveryPhase)
    }

    @Test
    fun userProfile_roundTrip_withDiscoveryPhase() {
        val wakeTime = Instant.parse("2026-05-15T06:00:00Z")
        val discoveryPhase = DiscoveryPhase(
            isActive = true,
            currentShift = ShiftType.LongerLatency,
            startDate = baseDate,
            weeklyRatings = listOf(
                DailyRating(
                    date = baseDate,
                    rating = 4,
                    shiftType = ShiftType.LongerLatency,
                    actualWakeUpTime = wakeTime,
                ),
                DailyRating(
                    date = baseDate.plusDays(1),
                    rating = null,
                    shiftType = ShiftType.LongerCycles,
                    actualWakeUpTime = null,
                ),
            ),
            isCompleted = false,
        )
        val original = UserProfile(userId = "user-1", discoveryPhase = discoveryPhase)

        val roundTripped = original.toEntity().toDomain()

        val result = roundTripped.discoveryPhase!!
        assertEquals(discoveryPhase.isActive, result.isActive)
        assertEquals(discoveryPhase.startDate, result.startDate)
        assertEquals(discoveryPhase.isCompleted, result.isCompleted)
        assertEquals(ShiftType.LongerLatency, result.currentShift)
        assertEquals(2, result.weeklyRatings.size)

        val firstRating = result.weeklyRatings[0]
        assertEquals(baseDate, firstRating.date)
        assertEquals(4, firstRating.rating)
        assertEquals(ShiftType.LongerLatency, firstRating.shiftType)
        assertEquals(wakeTime, firstRating.actualWakeUpTime)

        val secondRating = result.weeklyRatings[1]
        assertEquals(baseDate.plusDays(1), secondRating.date)
        assertNull(secondRating.rating)
        assertEquals(ShiftType.LongerCycles, secondRating.shiftType)
        assertNull(secondRating.actualWakeUpTime)
    }

    @Test
    fun userProfile_roundTrip_allShiftTypes() {
        listOf(ShiftType.LongerLatency, ShiftType.LongerCycles, ShiftType.FewerCycles).forEach { shift ->
            val profile = UserProfile(
                userId = "user-1",
                discoveryPhase = DiscoveryPhase(
                    isActive = true,
                    currentShift = shift,
                    startDate = baseDate,
                ),
            )
            val result = profile.toEntity().toDomain()
            assertEquals(shift, result.discoveryPhase?.currentShift)
        }
    }

    @Test
    fun userProfileEntity_toDomain_withNullDiscoveryPhase() {
        val profile = UserProfile(userId = "user-1", discoveryPhase = null)
        val entity = profile.toEntity()
        assertNull(entity.discoveryPhaseJson)
        assertNull(entity.toDomain().discoveryPhase)
    }
}
