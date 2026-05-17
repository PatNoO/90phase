package com.example.a90phase.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyCheckInScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(timeString: String) {
        val (hour, minute) = parseTime(timeString)
        val triggerMillis = nextTriggerMillis(hour, minute)
        val pending = buildPendingIntent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
        }
    }

    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        val todayAt = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        val target = if (now.isBefore(todayAt)) todayAt else todayAt.plusDays(1)
        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, DailyCheckInReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQUEST_CODE = 1001
    }
}
