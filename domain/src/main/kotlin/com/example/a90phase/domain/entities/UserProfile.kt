package com.example.a90phase.domain.entities

import com.example.a90phase.domain.common.DomainConstants

data class UserProfile(
    val userId: String,
    val email: String? = null,
    val displayName: String? = null,
    val optimalCycleMinutes: Int = DomainConstants.CYCLE_DURATION_MINUTES,
    val sleepLatencyMinutes: Int = DomainConstants.SLEEP_LATENCY_MINUTES,
    val preferredCycleCount: Int = 6,
    val reminderTime: String = "18:00",
    val notificationsEnabled: Boolean = true,
    val discoveryPhase: DiscoveryPhase? = null,
) {
    fun isDiscoveryPhaseActive(): Boolean = discoveryPhase?.isActive == true
}
