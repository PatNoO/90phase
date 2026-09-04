package com.example.a90phase.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Re-arms every enabled notification on app start.
 *
 * Runs as a worker rather than straight from `Application.onCreate` because reconciliation is
 * suspending work and the Application class must not own a `CoroutineScope` (CLAUDE.md § Key
 * Business Rules). WorkManager also means a start that is interrupted still completes later.
 */
class NotificationReconciliationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationReconciliationEntryPoint {
        fun notificationReconciler(): NotificationReconciler
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationReconciliationEntryPoint::class.java,
        )
        entryPoint.notificationReconciler().reconcile()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "notification_reconciliation"
    }
}
