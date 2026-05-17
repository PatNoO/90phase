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
class MorningFeedbackScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(wakeTime: LocalTime) {
        val triggerMillis = nextTriggerMillis(wakeTime)
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

    private fun nextTriggerMillis(wakeTime: LocalTime): Long {
        val now = LocalDateTime.now()
        val target = LocalDateTime.of(LocalDate.now(), wakeTime).plusMinutes(OFFSET_MINUTES)
        return (if (now.isBefore(target)) target else target.plusDays(1))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, MorningFeedbackReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQUEST_CODE = 1003
        private const val OFFSET_MINUTES = 15L
    }
}
