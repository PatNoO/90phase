package com.example.a90phase.data.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.workers.SleepLogSyncWorker
import com.example.a90phase.data.workers.SmartWakeMonitorWorker
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
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SleepLogSyncWorker.WORK_TAG, ExistingWorkPolicy.KEEP, request)
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
