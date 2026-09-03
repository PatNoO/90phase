package com.example.a90phase.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.a90phase.AlarmRingActivity
import com.example.a90phase.R
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fires at the scheduled wake time. Posts a high-importance full-screen-intent
 * notification that launches [AlarmRingActivity] over the lock screen, and re-arms
 * the alarm for the next day.
 */
@AndroidEntryPoint
class WakeAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    @Inject lateinit var wakeAlarmScheduler: WakeAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        // Ring first — the user is waiting on this, and the re-arm can finish behind it.
        showAlarm(context)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                rearmForNextDay()
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Re-arms here rather than in [AlarmRingActivity]'s Dismiss handler so tomorrow's alarm
     * survives the user never pressing anything — letting the screen time out, swiping the
     * ringing screen away, or silencing the phone and going back to sleep.
     */
    private suspend fun rearmForNextDay() {
        if (!userPreferencesDataStore.observeWakeAlarmEnabled().first()) return
        val hour = userPreferencesDataStore.observeSelectedWakeHour().first()
        val minute = userPreferencesDataStore.observeSelectedWakeMinute().first()
        wakeAlarmScheduler.rearmForNextDay(LocalTime.of(hour, minute))
    }

    private fun showAlarm(context: Context) {
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
