package com.example.a90phase.data.remote.firebase

import com.google.firebase.Timestamp

/**
 * Firestore DTO for users/{userId}/profile.
 *
 * All fields have defaults so Firestore can deserialize with toObject<T>().
 * Types chosen for Firestore compatibility: Long for integers, Timestamp for instants.
 */
data class FirestoreUserProfileDocument(
    val userId: String = "",
    val optimalCycleMinutes: Long = 90L,
    val sleepLatencyMinutes: Long = 15L,
    val preferredCycleCount: Long = 6L,
    val reminderTime: String = "18:00",
    val notificationsEnabled: Boolean = true,
    val smartWakeWindowEnabled: Boolean = false,
    // Nested map — keys match FirestoreDiscoveryPhaseDocument field names
    val discoveryPhase: Map<String, Any?>? = null,
    val updatedAt: Timestamp = Timestamp.now(),
)
