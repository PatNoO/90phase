package com.example.a90phase

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.sync.SyncScheduler
import com.example.a90phase.data.workers.FirebaseAuthWorker
import com.example.a90phase.notifications.NotificationChannels
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
        syncScheduler.schedulePeriodicFirebaseSync()
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
