package com.example.a90phase.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.a90phase.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the app's own wake alarm via [AlarmManager.setAlarmClock] — the API for
 * user-facing alarms. It is exempt from Doze, shows the system alarm icon, and appears
 * in `nextAlarmClock` so the calculator's alarm banner reflects it.
 */
@Singleton
class WakeAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(wakeTime: LocalTime) {
        val now = LocalDateTime.now()
        val todayAt = LocalDateTime.of(LocalDate.now(), wakeTime)
        val target = if (now.isBefore(todayAt)) todayAt else todayAt.plusDays(1)
        scheduleAt(target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }

    fun snooze(minutes: Long) {
        scheduleAt(System.currentTimeMillis() + minutes * MILLIS_PER_MINUTE)
    }

    fun cancel() {
        alarmManager.cancel(buildAlarmIntent())
    }

    private fun scheduleAt(triggerMillis: Long) {
        val info = AlarmManager.AlarmClockInfo(triggerMillis, buildShowIntent())
        alarmManager.setAlarmClock(info, buildAlarmIntent())
    }

    private fun buildAlarmIntent(): PendingIntent {
        val intent = Intent(context, WakeAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildShowIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            SHOW_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 1005
        private const val SHOW_REQUEST_CODE = 1006
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
