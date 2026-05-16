package com.example.a90phase.data.mapper

import com.example.a90phase.data.local.datastore.toJson
import com.example.a90phase.data.local.datastore.toUserOnboardingState
import com.example.a90phase.domain.entities.UserOnboardingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** JVM unit tests for OnboardingStateSerializer (pure Kotlin, no org.json dependency). */
class OnboardingStateSerializerTest {

    @Test
    fun roundTrip_defaultState() {
        val state = UserOnboardingState()
        assertEquals(state, state.toJson().toUserOnboardingState())
    }

    @Test
    fun roundTrip_allFlagsTrue() {
        val state = UserOnboardingState(
            isCompleted = true,
            dailyCheckInEnabled = true,
            bedtimeReminderEnabled = true,
            morningRatingEnabled = true,
            morningBedtimeLogEnabled = true,
            smartWakeWindowEnabled = true,
            discoveryPhaseInfoShown = true,
        )
        assertEquals(state, state.toJson().toUserOnboardingState())
    }

    @Test
    fun roundTrip_mixedFlags() {
        val state = UserOnboardingState(
            isCompleted = true,
            dailyCheckInEnabled = false,
            bedtimeReminderEnabled = true,
            morningRatingEnabled = false,
            morningBedtimeLogEnabled = false,
            smartWakeWindowEnabled = true,
            discoveryPhaseInfoShown = false,
        )
        assertEquals(state, state.toJson().toUserOnboardingState())
    }

    @Test
    fun corruptJson_returnsDefaultState() {
        val result = "not-valid-json".toUserOnboardingState()
        assertFalse(result.isCompleted)
        assertFalse(result.dailyCheckInEnabled)
    }

    @Test
    fun toJson_isCompletedTrue_containsCorrectValue() {
        val json = UserOnboardingState(isCompleted = true).toJson()
        assertTrue(json.contains("\"isCompleted\":true"))
    }
}
