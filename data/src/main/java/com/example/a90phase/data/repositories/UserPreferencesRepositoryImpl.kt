@file:Suppress("ForbiddenComment")

package com.example.a90phase.data.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.workers.SmartWakeMonitorWorker
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    // Smart Wake Window

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.setSmartWakeWindowEnabled(enabled)
            if (enabled) scheduleSmartWakeMonitor() else cancelSmartWakeMonitor()
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> =
        dataStore.observeSmartWakeWindowEnabled()

    // TODO: PH-24 — implement with Room once database is set up
    override suspend fun getUserProfile(): Result<UserProfile> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun setCycleDuration(minutes: Int): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun setSleepLatency(minutes: Int): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun setReminderTime(time: String): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override suspend fun endDiscoveryPhase(): Result<Unit> =
        Result.Error(DomainError.DatabaseError("Room not yet configured — PH-24"))

    // TODO: PH-24
    override fun observeUserProfile(): Flow<UserProfile> =
        kotlinx.coroutines.flow.emptyFlow()

    private fun scheduleSmartWakeMonitor() {
        val request = OneTimeWorkRequestBuilder<SmartWakeMonitorWorker>()
            .addTag(SmartWakeMonitorWorker.WORK_TAG)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SmartWakeMonitorWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, request)
    }

    private fun cancelSmartWakeMonitor() {
        WorkManager.getInstance(context).cancelAllWorkByTag(SmartWakeMonitorWorker.WORK_TAG)
    }
}
