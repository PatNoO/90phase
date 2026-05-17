package com.example.a90phase.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit
import com.example.a90phase.data.workers.SleepLogSyncWorker
import com.example.a90phase.data.workers.SmartWakeMonitorWorker
import com.example.a90phase.data.workers.UserProfileSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncScheduler {

    override fun enqueueSleepLogSync() {
        val request = OneTimeWorkRequestBuilder<SleepLogSyncWorker>()
            .addTag(SleepLogSyncWorker.WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SleepLogSyncWorker.WORK_TAG, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    override fun enqueueUserProfileSync() {
        val request = OneTimeWorkRequestBuilder<UserProfileSyncWorker>()
            .addTag(UserProfileSyncWorker.WORK_TAG)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UserProfileSyncWorker.WORK_TAG, ExistingWorkPolicy.KEEP, request)
    }

    override fun scheduleSmartWakeMonitor() {
        val request = OneTimeWorkRequestBuilder<SmartWakeMonitorWorker>()
            .addTag(SmartWakeMonitorWorker.WORK_TAG)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SmartWakeMonitorWorker.WORK_TAG, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancelSmartWakeMonitor() {
        WorkManager.getInstance(context).cancelAllWorkByTag(SmartWakeMonitorWorker.WORK_TAG)
    }
}
