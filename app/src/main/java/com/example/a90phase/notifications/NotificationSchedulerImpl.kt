package com.example.a90phase.notifications

import com.example.a90phase.domain.repositories.NotificationScheduler
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSchedulerImpl @Inject constructor(
    private val bedtimeReminderScheduler: BedtimeReminderScheduler,
    private val dailyCheckInScheduler: DailyCheckInScheduler,
) : NotificationScheduler {

    override fun scheduleBedtimeReminder(bedtime: LocalTime) = bedtimeReminderScheduler.schedule(bedtime)

    override fun cancelBedtimeReminder() = bedtimeReminderScheduler.cancel()

    override fun scheduleDailyCheckIn(timeString: String) = dailyCheckInScheduler.schedule(timeString)

    override fun cancelDailyCheckIn() = dailyCheckInScheduler.cancel()
}
