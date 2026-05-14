package com.example.a90phase

import android.app.Application
import com.example.a90phase.notifications.NotificationChannels
import com.example.a90phase.util.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NinetyPhaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(debug = BuildConfig.DEBUG)
        NotificationChannels.register(this)
    }
}
