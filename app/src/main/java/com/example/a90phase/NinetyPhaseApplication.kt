package com.example.a90phase

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.sync.SyncScheduler
import com.example.a90phase.data.workers.FirebaseAuthWorker
import com.example.a90phase.notifications.NotificationChannels
import com.example.a90phase.notifications.NotificationReconciliationWorker
import com.example.a90phase.util.AppLogger
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NinetyPhaseApplication : Application() {

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(debug = BuildConfig.DEBUG)
        NotificationChannels.register(this)
        enqueueFirebaseAuth()
        enqueueNotificationReconciliation()
        syncScheduler.schedulePeriodicFirebaseSync()
    }

    /**
     * Re-arms any notification whose alarm chain was broken while the app was not running — a
     * force-stop, an OEM battery killer, or a dropped alarm. Opening the app is then enough to
     * restore it, instead of waiting for a reboot or a Settings toggle.
     *
     * REPLACE, not KEEP: preferences may have changed since the last run, so the newest state
     * must win.
     */
    private fun enqueueNotificationReconciliation() {
        val request = OneTimeWorkRequestBuilder<NotificationReconciliationWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            NotificationReconciliationWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun enqueueFirebaseAuth() {
        val request = OneTimeWorkRequestBuilder<FirebaseAuthWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            FirebaseAuthWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
