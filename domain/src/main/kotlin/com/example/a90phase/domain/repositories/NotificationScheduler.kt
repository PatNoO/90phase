package com.example.a90phase.domain.repositories

import java.time.LocalTime

interface NotificationScheduler {
    fun scheduleBedtimeReminder(bedtime: LocalTime)

    fun cancelBedtimeReminder()

    fun scheduleDailyCheckIn(timeString: String)

    fun cancelDailyCheckIn()

    fun scheduleMorningFeedback(wakeTime: LocalTime)

    fun cancelMorningFeedback()

    fun scheduleWakeAlarm(wakeTime: LocalTime)

    fun cancelWakeAlarm()
}
