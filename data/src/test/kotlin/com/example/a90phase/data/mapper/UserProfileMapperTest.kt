package com.example.a90phase.data.mapper

import com.example.a90phase.data.local.room.mapper.toDomain
import com.example.a90phase.data.local.room.mapper.toEntity
import com.example.a90phase.domain.entities.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserProfileMapperTest {

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
    fun userProfile_nullDiscoveryPhase_producesNullJson() {
        val profile = UserProfile(userId = "user-1", discoveryPhase = null)
        val entity = profile.toEntity()
        assertNull(entity.discoveryPhaseJson)
        assertNull(entity.toDomain().discoveryPhase)
    }

    @Test
    fun userProfile_toEntity_usesRowIdZero() {
        assertEquals(0, UserProfile(userId = "user-1").toEntity().rowId)
    }

    @Test
    fun userProfile_defaults_arePreserved() {
        val profile = UserProfile(userId = "user-1")
        val roundTripped = profile.toEntity().toDomain()
        assertEquals(90, roundTripped.optimalCycleMinutes)
        assertEquals(15, roundTripped.sleepLatencyMinutes)
        assertEquals("18:00", roundTripped.reminderTime)
        assertEquals(true, roundTripped.notificationsEnabled)
        assertEquals(false, roundTripped.smartWakeWindowEnabled)
    }
}
