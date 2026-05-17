package com.example.a90phase.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SkipMorningFeedbackReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            context.getSystemService(NotificationManager::class.java).cancel(notificationId)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
