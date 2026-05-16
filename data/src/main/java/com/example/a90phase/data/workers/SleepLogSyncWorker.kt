@file:Suppress("ForbiddenComment")

package com.example.a90phase.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// TODO: EPIC-5 — implement Firebase sync logic here
class SleepLogSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = Result.success()

    companion object {
        const val WORK_TAG = "sleep_log_firebase_sync"
    }
}
