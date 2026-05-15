package com.example.a90phase.domain

import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UserProfileTest {
    @Test
    fun `default values match spec`() {
        val profile = UserProfile(userId = "test-id")

        assertEquals(90, profile.optimalCycleMinutes)
        assertEquals(15, profile.sleepLatencyMinutes)
        assertEquals(6, profile.preferredCycleCount)
        assertEquals("18:00", profile.reminderTime)
        assertTrue(profile.notificationsEnabled)
        assertFalse(profile.smartWakeWindowEnabled)
        assertFalse(profile.isDiscoveryPhaseActive())
    }

    @Test
    fun `isDiscoveryPhaseActive returns false when discoveryPhase is null`() {
        val profile = UserProfile(userId = "u1")
        assertFalse(profile.isDiscoveryPhaseActive())
    }

    @Test
    fun `isDiscoveryPhaseActive returns true when phase is active`() {
        val phase =
            DiscoveryPhase(
                isActive = true,
                currentShift = ShiftType.LongerLatency,
                startDate = LocalDate.now(),
            )
        val profile = UserProfile(userId = "u1", discoveryPhase = phase)
        assertTrue(profile.isDiscoveryPhaseActive())
    }

    @Test
    fun `isDiscoveryPhaseActive returns false when phase is inactive`() {
        val phase =
            DiscoveryPhase(
                isActive = false,
                currentShift = ShiftType.LongerLatency,
                startDate = LocalDate.now(),
            )
        val profile = UserProfile(userId = "u1", discoveryPhase = phase)
        assertFalse(profile.isDiscoveryPhaseActive())
    }

    @Test
    fun `copy produces new instance with changed field`() {
        val original = UserProfile(userId = "u1")
        val modified = original.copy(optimalCycleMinutes = 100)

        assertEquals(90, original.optimalCycleMinutes)
        assertEquals(100, modified.optimalCycleMinutes)
    }
}
