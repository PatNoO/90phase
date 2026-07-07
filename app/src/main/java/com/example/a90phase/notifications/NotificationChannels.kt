package com.example.a90phase.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val DAILY_CHECKIN_CHANNEL_ID = "daily_checkin"
    const val BEDTIME_REMINDER_CHANNEL_ID = "bedtime_reminder"
    const val MORNING_FEEDBACK_CHANNEL_ID = "morning_feedback"
    const val WAKE_ALARM_CHANNEL_ID = "wake_alarm"

    fun register(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dailyCheckin = NotificationChannel(
            DAILY_CHECKIN_CHANNEL_ID,
            "Daglig påminnelse",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Påminner dig att ange morgondagens väckningstid"
        }

        val bedtimeReminder = NotificationChannel(
            BEDTIME_REMINDER_CHANNEL_ID,
            "Läggdagspåminnelse",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Påminner dig om optimal läggdags baserat på din väckningstid"
        }

        val morningFeedback = NotificationChannel(
            MORNING_FEEDBACK_CHANNEL_ID,
            "Morgonfeedback",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Ber om din sömnkvalitetsbedömning efter att du vaknat"
            setSound(null, null)
        }

        val wakeAlarm = NotificationChannel(
            WAKE_ALARM_CHANNEL_ID,
            "Väckarklocka",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Ringer vid din väckningstid"
            // The ringing screen owns the sound/vibration, so keep the channel itself silent
            // to avoid a double sound.
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannels(listOf(dailyCheckin, bedtimeReminder, morningFeedback, wakeAlarm))
    }
}
