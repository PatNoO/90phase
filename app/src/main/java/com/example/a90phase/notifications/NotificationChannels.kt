package com.example.a90phase.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.a90phase.R

object NotificationChannels {

    const val DAILY_CHECKIN_CHANNEL_ID = "daily_checkin"
    const val BEDTIME_REMINDER_CHANNEL_ID = "bedtime_reminder"
    const val MORNING_FEEDBACK_CHANNEL_ID = "morning_feedback"
    const val WAKE_ALARM_CHANNEL_ID = "wake_alarm"

    fun register(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dailyCheckin = NotificationChannel(
            DAILY_CHECKIN_CHANNEL_ID,
            context.getString(R.string.channel_daily_checkin_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.channel_daily_checkin_description)
        }

        val bedtimeReminder = NotificationChannel(
            BEDTIME_REMINDER_CHANNEL_ID,
            context.getString(R.string.channel_bedtime_reminder_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_bedtime_reminder_description)
        }

        // IMPORTANCE_DEFAULT, not LOW: LOW means no sound *and* no heads-up banner, so the
        // rating prompt landed silently in the shade and was routinely missed entirely. Not
        // HIGH either — that is the wake alarm's level, and this prompt should not shout as
        // loudly as the alarm itself.
        //
        // Channel settings are frozen after first creation, so this only takes effect on a
        // fresh install. See the PH-94 ticket for why no `_v2` id was introduced.
        val morningFeedback = NotificationChannel(
            MORNING_FEEDBACK_CHANNEL_ID,
            context.getString(R.string.channel_morning_feedback_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.channel_morning_feedback_description)
        }

        val wakeAlarm = NotificationChannel(
            WAKE_ALARM_CHANNEL_ID,
            context.getString(R.string.channel_wake_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_wake_alarm_description)
            // The ringing screen owns the sound/vibration, so keep the channel itself silent
            // to avoid a double sound.
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannels(listOf(dailyCheckin, bedtimeReminder, morningFeedback, wakeAlarm))
    }
}
