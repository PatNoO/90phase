package com.example.a90phase.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    // Always 0 — this is a single-row table
    @PrimaryKey val rowId: Int = 0,
    @ColumnInfo(name = "user_id") val userId: String,
    val email: String?,
    @ColumnInfo(name = "display_name") val displayName: String?,
    @ColumnInfo(name = "optimal_cycle_minutes") val optimalCycleMinutes: Int,
    @ColumnInfo(name = "sleep_latency_minutes") val sleepLatencyMinutes: Int,
    @ColumnInfo(name = "preferred_cycle_count") val preferredCycleCount: Int,
    @ColumnInfo(name = "reminder_time") val reminderTime: String,
    @ColumnInfo(name = "notifications_enabled") val notificationsEnabled: Boolean,
    @ColumnInfo(name = "smart_wake_window_enabled") val smartWakeWindowEnabled: Boolean,
    @ColumnInfo(name = "discovery_phase_json") val discoveryPhaseJson: String?,
)
