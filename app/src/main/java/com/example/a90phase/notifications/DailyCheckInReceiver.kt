package com.example.a90phase.notifications

import android.app.AlarmManager
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyCheckInReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var dailyCheckInScheduler: DailyCheckInScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val reminderTime = userPreferencesDataStore.observeNotificationTime().first()
                // Re-schedule before checking enabled — never miss a day
                dailyCheckInScheduler.schedule(reminderTime)

                val enabled = userPreferencesDataStore.observeDailyCheckInEnabled().first()
                if (!enabled) return@launch

                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val nextAlarmTime = alarmManager.nextAlarmClock?.triggerTime?.let {
                    Instant.ofEpochMilli(it)
                }

                postNotification(context, nextAlarmTime)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context, alarmTime: Instant?) {
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

        val disableIntent = Intent(context, DisableCheckInReceiver::class.java)
        val disablePending = PendingIntent.getBroadcast(
            context,
            DISABLE_REQUEST_CODE,
            disableIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, NotificationChannels.DAILY_CHECKIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_checkin_title))
            .setContentText(context.getString(R.string.notification_checkin_body))
            .setContentIntent(contentPending)
            .setAutoCancel(true)
            .addAction(
                0,
                context.getString(R.string.notification_turn_off),
                disablePending,
            )

        if (alarmTime != null) {
            val timeLabel = alarmTime
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            builder
                .addAction(
                    0,
                    context.getString(R.string.notification_alarm_label, timeLabel),
                    contentPending,
                )
                .addAction(
                    0,
                    context.getString(R.string.notification_customize),
                    contentPending,
                )
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val CONTENT_REQUEST_CODE = 2002
        private const val DISABLE_REQUEST_CODE = 2003
    }
}
