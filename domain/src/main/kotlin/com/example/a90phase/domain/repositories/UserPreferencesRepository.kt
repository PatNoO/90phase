package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    // Smart Wake Window
    suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit>

    fun observeSmartWakeWindowEnabled(): Flow<Boolean>

    // Profile
    suspend fun getUserProfile(): Result<UserProfile>

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    // Preferences
    suspend fun setCycleDuration(minutes: Int): Result<Unit>

    suspend fun setSleepLatency(minutes: Int): Result<Unit>

    suspend fun setReminderTime(time: String): Result<Unit>

    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>

    // Discovery Phase
    suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit>

    suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit>

    suspend fun endDiscoveryPhase(): Result<Unit>

    // Reactive
    fun observeUserProfile(): Flow<UserProfile>
}
