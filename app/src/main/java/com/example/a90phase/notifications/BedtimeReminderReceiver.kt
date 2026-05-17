package com.example.a90phase.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.a90phase.MainActivity
import com.example.a90phase.R
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BedtimeReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val enabled = userPreferencesDataStore.observeBedtimeReminderEnabled().first()
                if (!enabled) return@launch

                val cycleCount = userPreferencesDataStore.observeSelectedBedtimeCycles().first()
                val durationMinutes = userPreferencesDataStore.observeSelectedBedtimeDuration().first()

                postNotification(context, cycleCount, durationMinutes)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context, cycleCount: Int, durationMinutes: Int) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            CONTENT_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        val durationLabel = if (minutes == 0) "${hours}h" else "${hours}h ${minutes}min"

        val notification = NotificationCompat.Builder(context, NotificationChannels.BEDTIME_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_bedtime_title))
            .setContentText(context.getString(R.string.notification_bedtime_body, cycleCount, durationLabel))
            .setContentIntent(contentPending)
            .setAutoCancel(true)
            .addAction(
                0,
                context.getString(R.string.notification_set_alarm),
                contentPending,
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 3001
        private const val CONTENT_REQUEST_CODE = 3002
    }
}
