package com.example.a90phase.notifications

import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Re-arms every enabled notification from stored preferences.
 *
 * All four notifications are one-shot `AlarmManager` alarms that re-arm themselves when they
 * fire. That chain breaks permanently the moment one link is lost — a force-stop, an OEM battery
 * killer, or the system dropping an alarm — and nothing brings it back until the next reboot or a
 * Settings toggle. Running this on every app start closes that hole: opening the app is enough to
 * restore anything that went missing.
 *
 * Shared by [BootReceiver] and [NotificationReconciliationWorker] so the two cannot drift.
 */
@Singleton
class NotificationReconciler @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val dailyCheckInScheduler: DailyCheckInScheduler,
    private val bedtimeReminderScheduler: BedtimeReminderScheduler,
    private val morningFeedbackScheduler: MorningFeedbackScheduler,
    private val wakeAlarmScheduler: WakeAlarmScheduler,
) {
    suspend fun reconcile() {
        reconcileDailyCheckIn()
        reconcileBedtimeReminder()
        reconcileMorningFeedback()
        reconcileWakeAlarm()
    }

    private suspend fun reconcileDailyCheckIn() {
        if (!userPreferencesDataStore.observeDailyCheckInEnabled().first()) return
        dailyCheckInScheduler.schedule(userPreferencesDataStore.observeNotificationTime().first())
    }

    private suspend fun reconcileBedtimeReminder() {
        if (!userPreferencesDataStore.observeBedtimeReminderEnabled().first()) return
        val hour = userPreferencesDataStore.observeSelectedBedtimeHour().first()
        val minute = userPreferencesDataStore.observeSelectedBedtimeMinute().first()
        bedtimeReminderScheduler.schedule(LocalTime.of(hour, minute))
    }

    private suspend fun reconcileMorningFeedback() {
        if (!userPreferencesDataStore.observeMorningRatingEnabled().first()) return
        morningFeedbackScheduler.schedule(selectedWakeTime())
    }

    private suspend fun reconcileWakeAlarm() {
        if (!userPreferencesDataStore.observeWakeAlarmEnabled().first()) return
        wakeAlarmScheduler.schedule(selectedWakeTime())
    }

    private suspend fun selectedWakeTime(): LocalTime {
        val hour = userPreferencesDataStore.observeSelectedWakeHour().first()
        val minute = userPreferencesDataStore.observeSelectedWakeMinute().first()
        return LocalTime.of(hour, minute)
    }
}
