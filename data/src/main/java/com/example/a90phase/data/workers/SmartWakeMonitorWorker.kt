@file:Suppress("ForbiddenComment")

package com.example.a90phase.data.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

// TODO: EPIC-4 — implement accelerometer monitoring logic here
class SmartWakeMonitorWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {

    override fun doWork(): Result = Result.success()

    companion object {
        const val WORK_TAG = "smart_wake_window_monitor"
    }
}
