package com.example.a90phase

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.workers.FirebaseAuthWorker
import com.example.a90phase.notifications.NotificationChannels
import com.example.a90phase.util.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NinetyPhaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(debug = BuildConfig.DEBUG)
        NotificationChannels.register(this)
        enqueueFirebaseAuth()
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
