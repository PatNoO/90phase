package com.example.a90phase.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import com.example.a90phase.data.BuildConfig
import androidx.work.WorkerParameters
import com.example.a90phase.data.remote.firebase.FirebaseSleepLogDataSource
import com.example.a90phase.data.remote.firebase.FirebaseUserProfileDataSource
import com.example.a90phase.data.remote.firebase.toDomainSleepLog
import com.example.a90phase.data.remote.firebase.toFirestoreDocument
import com.example.a90phase.data.sync.ConflictResolution
import com.example.a90phase.data.sync.SleepLogConflictResolver
import com.example.a90phase.domain.common.Result as DomainResult
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.AuthRepository
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import kotlinx.coroutines.flow.first

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
            debugLog(TAG, "No authenticated user — skipping sync")
            return Result.retry()
        }

        uploadPendingSleepLogs(userId, sleepRepository)
        syncUserProfile(userId, prefsRepository)
        resolveRemoteConflicts(userId, sleepRepository)

        return Result.success()
    }

    private suspend fun uploadPendingSleepLogs(userId: String, sleepRepository: SleepRepository) {
        val pendingLogs = when (val r = sleepRepository.getPendingUploadLogs()) {
            is DomainResult.Success -> r.data
            else -> {
                debugLog(TAG, "Failed to load pending logs from Room")
                return
            }
        }

        var syncedCount = 0
        var failedCount = 0
        for (log in pendingLogs) {
            when (sleepLogDataSource.uploadSleepLog(userId, log.toFirestoreDocument())) {
                is DomainResult.Success -> {
                    sleepRepository.updateSyncStatus(log.id, SyncStatus.SYNCED)
                    syncedCount++
                }
                else -> failedCount++
            }
        }
        debugLog(TAG, "Upload: $syncedCount synced, $failedCount failed (will retry next run)")
    }

    private suspend fun syncUserProfile(userId: String, prefsRepository: UserPreferencesRepository) {
        val profile = when (val r = prefsRepository.getUserProfile()) {
            is DomainResult.Success -> r.data
            else -> return
        }
        if (profile.userId == LOCAL_USER_ID) return
        when (profileDataSource.uploadProfile(userId, profile.copy(userId = userId).toFirestoreDocument())) {
            is DomainResult.Success -> debugLog(TAG, "Profile synced")
            else -> debugLog(TAG, "Profile sync failed — will retry next run")
        }
    }

    /**
     * Downloads Firestore logs updated since the last sync and applies conflict resolution.
     * Firestore wins only when its updatedAt is strictly newer than Room's version.
     */
    private suspend fun resolveRemoteConflicts(userId: String, sleepRepository: SleepRepository) {
        val since = when (val r = sleepRepository.getLastSyncTimestamp()) {
            is DomainResult.Success -> r.data
            else -> Instant.EPOCH
        }

        val remoteLogs = when (val r = sleepLogDataSource.downloadSleepLogsUpdatedAfter(userId, since)) {
            is DomainResult.Success -> r.data
            else -> {
                debugLog(TAG, "Failed to fetch remote logs for conflict resolution")
                return
            }
        }

        var localWins = 0
        var remoteWins = 0

        for (remote in remoteLogs) {
            val remoteUpdatedAt = Instant.ofEpochSecond(
                remote.updatedAt.seconds,
                remote.updatedAt.nanoseconds.toLong(),
            )
            val local = sleepRepository.getSleepLog(remote.id).first()

            if (local == null) {
                // Log exists remotely but not locally — always take remote
                sleepRepository.saveSleepLog(remote.toDomainSleepLog())
                remoteWins++
                continue
            }

            when (SleepLogConflictResolver.resolve(local.updatedAt, remoteUpdatedAt)) {
                ConflictResolution.LOCAL_WINS -> {
                    debugLog(TAG, "Conflict log=${remote.id}: LOCAL wins")
                    localWins++
                }
                ConflictResolution.REMOTE_WINS -> {
                    debugLog(TAG, "Conflict log=${remote.id}: REMOTE wins — updating Room")
                    sleepRepository.saveSleepLog(remote.toDomainSleepLog())
                    remoteWins++
                }
            }
        }

        if (localWins + remoteWins > 0) {
            debugLog(TAG, "Conflict resolution: local=$localWins remote=$remoteWins")
        }

        sleepRepository.updateLastSyncTimestamp(Instant.now())
    }

    companion object {
        const val WORK_TAG = "firebase_periodic_sync"
        const val WORK_NAME = "firebase_periodic_sync"
        private const val LOCAL_USER_ID = "local_user"
        private const val TAG = "FirebaseSyncWorker"
    }
}

private fun debugLog(tag: String, message: String) {
    if (BuildConfig.DEBUG) Log.d(tag, message)
}
