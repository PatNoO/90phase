package com.example.a90phase.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.a90phase.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
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
        scheduleDailyAt(nextWakeOccurrence(wakeTime, now).toEpochMillis())
    }

    /**
     * Re-arms the daily alarm after it has just fired, so the alarm repeats every day the way a
     * phone's own alarm does.
     *
     * Unlike [schedule] this refuses to target a time that is essentially now. The receiver runs
     * at (or a hair before) the trigger instant, and [schedule] would then read "today's wake time
     * is still ahead" and re-arm for a few milliseconds later — ringing the user in a loop.
     */
    fun rearmForNextDay(wakeTime: LocalTime) {
        val now = LocalDateTime.now()
        val target = nextWakeOccurrence(wakeTime, now, notBefore = now.plusMinutes(REARM_GUARD_MINUTES))
        scheduleDailyAt(target.toEpochMillis())
    }

    /**
     * Snoozes on its own request code so it never overwrites the daily alarm. Sharing one code
     * would mean a snooze silently replaced tomorrow's alarm, and dismissing after a snooze would
     * leave the user with no alarm at all.
     */
    fun snooze(minutes: Long) {
        val triggerMillis = System.currentTimeMillis() + minutes * MILLIS_PER_MINUTE
        val info = AlarmManager.AlarmClockInfo(triggerMillis, buildShowIntent())
        alarmManager.setAlarmClock(info, buildSnoozeIntent())
    }

    fun cancelSnooze() {
        alarmManager.cancel(buildSnoozeIntent())
    }

    /** Cancels the daily alarm and any pending snooze — used when the alarm is switched off. */
    fun cancel() {
        alarmManager.cancel(buildAlarmIntent())
        cancelSnooze()
    }

    private fun scheduleDailyAt(triggerMillis: Long) {
        val info = AlarmManager.AlarmClockInfo(triggerMillis, buildShowIntent())
        alarmManager.setAlarmClock(info, buildAlarmIntent())
    }

    private fun buildAlarmIntent(): PendingIntent = buildReceiverIntent(ALARM_REQUEST_CODE)

    private fun buildSnoozeIntent(): PendingIntent = buildReceiverIntent(SNOOZE_REQUEST_CODE)

    private fun buildReceiverIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, WakeAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
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
        private const val SNOOZE_REQUEST_CODE = 1007
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val REARM_GUARD_MINUTES = 1L
    }
}

/**
 * The next date-time at which [wakeTime] occurs, strictly after [notBefore].
 *
 * [notBefore] defaults to [now], which is what plain scheduling wants. Re-arming after the alarm
 * has just fired passes a slightly later bound instead: the receiver runs at (or a hair before)
 * the trigger instant, and without that guard "today's wake time" still reads as being in the
 * future, re-arming the alarm milliseconds later and ringing the user in a loop.
 */
internal fun nextWakeOccurrence(
    wakeTime: LocalTime,
    now: LocalDateTime,
    notBefore: LocalDateTime = now,
): LocalDateTime {
    val todayAt = LocalDateTime.of(now.toLocalDate(), wakeTime)
    return if (todayAt.isAfter(notBefore)) todayAt else todayAt.plusDays(1)
}

private fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
