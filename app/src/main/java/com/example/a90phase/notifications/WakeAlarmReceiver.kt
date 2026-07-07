package com.example.a90phase.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.a90phase.AlarmRingActivity
import com.example.a90phase.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fires at the scheduled wake time. Posts a high-importance full-screen-intent
 * notification that launches [AlarmRingActivity] over the lock screen.
 */
@AndroidEntryPoint
class WakeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPending = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.WAKE_ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentText(context.getString(R.string.alarm_notification_body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPending, true)
            .setContentIntent(fullScreenPending)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)

        // Also start the activity directly — on unlocked screens the full-screen intent
        // may only post a heads-up notification.
        context.startActivity(fullScreenIntent)
    }

    companion object {
        const val NOTIFICATION_ID = 5001
        private const val REQUEST_CODE = 5002
    }
}
