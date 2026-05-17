package com.example.a90phase.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a90phase.data.remote.firebase.FirebaseSleepLogDataSource
import com.example.a90phase.data.remote.firebase.toFirestoreDocument
import com.example.a90phase.domain.common.Result as DomainResult
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.AuthRepository
import com.example.a90phase.domain.repositories.SleepRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SleepLogSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SleepLogSyncWorkerEntryPoint {
        fun sleepRepository(): SleepRepository
        fun authRepository(): AuthRepository
    }

    private val dataSource = FirebaseSleepLogDataSource()

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SleepLogSyncWorkerEntryPoint::class.java,
        )
        val sleepRepository = entryPoint.sleepRepository()
        val authRepository = entryPoint.authRepository()

        val userId = authRepository.getCurrentUserId() ?: return Result.retry()

        val pendingLogs = when (val r = sleepRepository.getPendingUploadLogs()) {
            is DomainResult.Success -> r.data
            else -> return Result.retry()
        }

        if (pendingLogs.isEmpty()) return Result.success()

        for (log in pendingLogs) {
            when (dataSource.uploadSleepLog(userId, log.toFirestoreDocument())) {
                is DomainResult.Success ->
                    sleepRepository.updateSyncStatus(log.id, SyncStatus.SYNCED)
                else -> return Result.retry()
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_TAG = "sleep_log_firebase_sync"
    }
}
