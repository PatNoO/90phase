package com.example.a90phase.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {

    const val DAILY_CHECKIN_CHANNEL_ID = "daily_checkin"

    fun register(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val dailyCheckin = NotificationChannel(
            DAILY_CHECKIN_CHANNEL_ID,
            "Daglig påminnelse",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Påminner dig att ange morgondagens väckningstid"
        }

        manager.createNotificationChannel(dailyCheckin)
    }
}
