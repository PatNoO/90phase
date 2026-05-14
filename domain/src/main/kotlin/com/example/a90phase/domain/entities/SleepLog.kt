package com.example.a90phase.domain.entities

import java.time.Instant
import java.time.LocalDate

data class SleepLog(
    val id: String,
    val date: LocalDate,
    val bedtime: Instant? = null,
    val wakeTime: Instant,
    val qualityRating: Int? = null,
    val cycleCount: Int,
    val cycleDurationUsed: Int,
    val sleepLatencyUsed: Int,
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD,
) {
    fun hasBeenRated(): Boolean = qualityRating != null
}

enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DOWNLOAD,
    CONFLICT,
}
