package com.example.a90phase.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var dailyCheckInScheduler: DailyCheckInScheduler
    @Inject lateinit var bedtimeReminderScheduler: BedtimeReminderScheduler
    @Inject lateinit var morningFeedbackScheduler: MorningFeedbackScheduler
    @Inject lateinit var wakeAlarmScheduler: WakeAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                rescheduleDailyCheckIn()
                rescheduleBedtimeReminder()
                rescheduleMorningFeedback()
                rescheduleWakeAlarm()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun rescheduleDailyCheckIn() {
        val enabled = userPreferencesDataStore.observeDailyCheckInEnabled().first()
        if (!enabled) return
        val time = userPreferencesDataStore.observeNotificationTime().first()
        dailyCheckInScheduler.schedule(time)
    }

    private suspend fun rescheduleBedtimeReminder() {
        val enabled = userPreferencesDataStore.observeBedtimeReminderEnabled().first()
        if (!enabled) return
        val hour = userPreferencesDataStore.observeSelectedBedtimeHour().first()
        val minute = userPreferencesDataStore.observeSelectedBedtimeMinute().first()
        bedtimeReminderScheduler.schedule(LocalTime.of(hour, minute))
    }

    private suspend fun rescheduleMorningFeedback() {
        val enabled = userPreferencesDataStore.observeMorningRatingEnabled().first()
        if (!enabled) return
        val hour = userPreferencesDataStore.observeSelectedWakeHour().first()
        val minute = userPreferencesDataStore.observeSelectedWakeMinute().first()
        morningFeedbackScheduler.schedule(LocalTime.of(hour, minute))
    }

    private suspend fun rescheduleWakeAlarm() {
        val enabled = userPreferencesDataStore.observeWakeAlarmEnabled().first()
        if (!enabled) return
        val hour = userPreferencesDataStore.observeSelectedWakeHour().first()
        val minute = userPreferencesDataStore.observeSelectedWakeMinute().first()
        wakeAlarmScheduler.schedule(LocalTime.of(hour, minute))
    }
}
