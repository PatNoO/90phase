package com.example.a90phase.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisableCheckInReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var dailyCheckInScheduler: DailyCheckInScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                userPreferencesDataStore.setDailyCheckInEnabled(false)
                dailyCheckInScheduler.cancel()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
