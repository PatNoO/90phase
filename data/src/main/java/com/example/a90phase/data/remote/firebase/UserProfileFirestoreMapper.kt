package com.example.a90phase.data.remote.firebase

import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import com.google.firebase.Timestamp
import java.time.LocalDate

fun UserProfile.toFirestoreDocument(): FirestoreUserProfileDocument =
    FirestoreUserProfileDocument(
        userId = userId,
        optimalCycleMinutes = optimalCycleMinutes.toLong(),
        sleepLatencyMinutes = sleepLatencyMinutes.toLong(),
        preferredCycleCount = preferredCycleCount.toLong(),
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        smartWakeWindowEnabled = smartWakeWindowEnabled,
        discoveryPhase = discoveryPhase?.toFirestoreMap(),
        updatedAt = Timestamp.now(),
    )

fun FirestoreUserProfileDocument.toDomain(): UserProfile =
    UserProfile(
        userId = userId,
        optimalCycleMinutes = optimalCycleMinutes.toInt(),
        sleepLatencyMinutes = sleepLatencyMinutes.toInt(),
        preferredCycleCount = preferredCycleCount.toInt(),
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        smartWakeWindowEnabled = smartWakeWindowEnabled,
        discoveryPhase = discoveryPhase?.toDiscoveryPhase(),
    )

private fun DiscoveryPhase.toFirestoreMap(): Map<String, Any?> =
    mapOf(
        "isActive" to isActive,
        "startDate" to startDate.toString(),
        "currentShift" to currentShift.toKey(),
        "isCompleted" to isCompleted,
    )

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.toDiscoveryPhase(): DiscoveryPhase? =
    runCatching {
        DiscoveryPhase(
            isActive = get("isActive") as? Boolean ?: return null,
            startDate = LocalDate.parse(get("startDate") as? String ?: return null),
            currentShift = (get("currentShift") as? String)?.toShiftType() ?: return null,
            isCompleted = get("isCompleted") as? Boolean ?: false,
        )
    }.getOrNull()

private fun ShiftType.toKey(): String =
    when (this) {
        is ShiftType.LongerLatency -> "LONGER_LATENCY"
        is ShiftType.LongerCycles -> "LONGER_CYCLES"
        is ShiftType.FewerCycles -> "FEWER_CYCLES"
    }

private fun String.toShiftType(): ShiftType =
    when (this) {
        "LONGER_LATENCY" -> ShiftType.LongerLatency
        "LONGER_CYCLES" -> ShiftType.LongerCycles
        "FEWER_CYCLES" -> ShiftType.FewerCycles
        else -> ShiftType.LongerLatency
    }
