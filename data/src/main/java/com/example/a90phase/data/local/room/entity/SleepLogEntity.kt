package com.example.a90phase.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.a90phase.domain.entities.SyncStatus
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "sleep_logs",
    indices = [
        Index(value = ["wake_time"]),
        Index(value = ["quality_rating"]),
        Index(value = ["sync_status"]),
    ],
)
data class SleepLogEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val bedtime: Instant?,
    @ColumnInfo(name = "wake_time") val wakeTime: Instant,
    @ColumnInfo(name = "quality_rating") val qualityRating: Int?,
    @ColumnInfo(name = "cycle_count") val cycleCount: Int,
    @ColumnInfo(name = "cycle_duration_used") val cycleDurationUsed: Int,
    @ColumnInfo(name = "sleep_latency_used") val sleepLatencyUsed: Int,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "sync_status") val syncStatus: SyncStatus,
)
