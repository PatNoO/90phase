package com.example.a90phase.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.a90phase.MorningBedtimeLogActivity
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.domain.common.DomainConstants
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.AnalyzeDiscoveryPhaseUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RatingActionReceiver : BroadcastReceiver() {

    @Inject lateinit var sleepRepository: SleepRepository
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val rating = intent.getIntExtra(EXTRA_RATING, -1)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (rating == -1) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val bedtimeLogEnabled = userPreferencesDataStore.observeMorningBedtimeLogEnabled().first()

                if (bedtimeLogEnabled) {
                    val activityIntent = Intent(context, MorningBedtimeLogActivity::class.java).apply {
                        putExtra(MorningBedtimeLogActivity.EXTRA_RATING, rating)
                        putExtra(MorningBedtimeLogActivity.EXTRA_NOTIFICATION_ID, notificationId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(activityIntent)
                    dismissNotification(context, notificationId)
                } else {
                    saveRatingAndDismiss(context, rating, notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun saveRatingAndDismiss(context: Context, rating: Int, notificationId: Int) {
        val profileResult = userPreferencesRepository.getUserProfile()
        val profile = (profileResult as? Result.Success)?.data

        val wakeHour = userPreferencesDataStore.observeSelectedWakeHour().first()
        val wakeMinute = userPreferencesDataStore.observeSelectedWakeMinute().first()
        val cycleCount = userPreferencesDataStore.observeSelectedBedtimeCycles().first()
        val wakeInstant = LocalDate.now()
            .atTime(wakeHour, wakeMinute)
            .atZone(ZoneId.systemDefault())
            .toInstant()

        val log = SleepLog(
            id = UUID.randomUUID().toString(),
            date = LocalDate.now(),
            wakeTime = wakeInstant,
            qualityRating = rating,
            cycleCount = cycleCount,
            cycleDurationUsed = profile?.optimalCycleMinutes ?: DomainConstants.CYCLE_DURATION_MINUTES,
            sleepLatencyUsed = profile?.sleepLatencyMinutes ?: DomainConstants.SLEEP_LATENCY_MINUTES,
            syncStatus = SyncStatus.PENDING_UPLOAD,
        )
        sleepRepository.saveSleepLog(log)
        dismissNotification(context, notificationId)

        if (profile?.isDiscoveryPhaseActive() == true) {
            AnalyzeDiscoveryPhaseUseCase(userPreferencesRepository).invoke()
        }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId != -1) {
            context.getSystemService(NotificationManager::class.java).cancel(notificationId)
        }
    }

    companion object {
        const val EXTRA_RATING = "extra_rating"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
