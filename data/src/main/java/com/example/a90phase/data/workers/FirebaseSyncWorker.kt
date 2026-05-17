package com.example.a90phase.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a90phase.data.remote.firebase.FirebaseSleepLogDataSource
import com.example.a90phase.data.remote.firebase.FirebaseUserProfileDataSource
import com.example.a90phase.data.remote.firebase.toFirestoreDocument
import com.example.a90phase.domain.common.Result as DomainResult
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.AuthRepository
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class FirebaseSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FirebaseSyncWorkerEntryPoint {
        fun sleepRepository(): SleepRepository
        fun userPreferencesRepository(): UserPreferencesRepository
        fun authRepository(): AuthRepository
    }

    private val sleepLogDataSource = FirebaseSleepLogDataSource()
    private val profileDataSource = FirebaseUserProfileDataSource()

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            FirebaseSyncWorkerEntryPoint::class.java,
        )
        val sleepRepository = entryPoint.sleepRepository()
        val prefsRepository = entryPoint.userPreferencesRepository()
        val authRepository = entryPoint.authRepository()

        val userId = authRepository.getCurrentUserId() ?: run {
            Log.d(TAG, "No authenticated user — skipping sync")
            return Result.retry()
        }

        var syncedCount = 0
        var failedCount = 0

        // Sync pending sleep logs — continue on individual failures (partial-failure handling)
        val pendingLogs = when (val r = sleepRepository.getPendingUploadLogs()) {
            is DomainResult.Success -> r.data
            else -> {
                Log.d(TAG, "Failed to load pending logs from Room")
                emptyList()
            }
        }

        for (log in pendingLogs) {
            when (sleepLogDataSource.uploadSleepLog(userId, log.toFirestoreDocument())) {
                is DomainResult.Success -> {
                    sleepRepository.updateSyncStatus(log.id, SyncStatus.SYNCED)
                    syncedCount++
                }
                else -> failedCount++
            }
        }

        // Sync user profile
        val profile = when (val r = prefsRepository.getUserProfile()) {
            is DomainResult.Success -> r.data
            else -> null
        }

        if (profile != null && profile.userId != LOCAL_USER_ID) {
            when (profileDataSource.uploadProfile(userId, profile.copy(userId = userId).toFirestoreDocument())) {
                is DomainResult.Success -> Log.d(TAG, "Profile synced")
                else -> Log.d(TAG, "Profile sync failed — will retry next run")
            }
        }

        Log.d(TAG, "Sync complete: $syncedCount synced, $failedCount failed (will retry next run)")

        // Always succeed — failed items remain PENDING and are picked up on the next run
        return Result.success()
    }

    companion object {
        const val WORK_TAG = "firebase_periodic_sync"
        const val WORK_NAME = "firebase_periodic_sync"
        private const val LOCAL_USER_ID = "local_user"
        private const val TAG = "FirebaseSyncWorker"
    }
}
