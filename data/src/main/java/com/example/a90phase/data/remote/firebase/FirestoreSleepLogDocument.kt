package com.example.a90phase.data.remote.firebase

import com.google.firebase.Timestamp

/**
 * Firestore DTO for users/{userId}/sleep_logs/{logId}.
 *
 * Document ID in Firestore matches the Room primary key (id field).
 * All fields have defaults so Firestore can deserialize with toObject<T>().
 *
 * date is stored as an ISO string ("yyyy-MM-dd") — Firestore has no native date-only type.
 * bedtime and wakeTime are stored as Timestamps (epoch-based, timezone-neutral).
 */
data class FirestoreSleepLogDocument(
    val id: String = "",
    val date: String = "",
    val bedtime: Timestamp? = null,
    val wakeTime: Timestamp = Timestamp(0, 0),
    val qualityRating: Long? = null,
    val cycleCount: Long = 0L,
    val cycleDurationUsed: Long = 90L,
    val sleepLatencyUsed: Long = 15L,
    val notes: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
)
