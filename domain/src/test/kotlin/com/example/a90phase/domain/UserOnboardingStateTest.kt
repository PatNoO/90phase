package com.example.a90phase.domain

import com.example.a90phase.domain.entities.UserOnboardingState
import org.junit.Assert.assertFalse
import org.junit.Test

class UserOnboardingStateTest {
    @Test
    fun `all fields default to false`() {
        val state = UserOnboardingState()

        assertFalse(state.isCompleted)
        assertFalse(state.dailyCheckInEnabled)
        assertFalse(state.bedtimeReminderEnabled)
        assertFalse(state.morningRatingEnabled)
        assertFalse(state.morningBedtimeLogEnabled)
        assertFalse(state.smartWakeWindowEnabled)
        assertFalse(state.discoveryPhaseInfoShown)
    }

    @Test
    fun `copy produces independent instance`() {
        val original = UserOnboardingState()
        original.copy(dailyCheckInEnabled = true, isCompleted = true)

        assertFalse(original.dailyCheckInEnabled)
        assertFalse(original.isCompleted)
    }
}
