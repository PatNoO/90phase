package com.example.a90phase.notifications

import android.app.NotificationManager
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
class DisableMorningFeedbackReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                userPreferencesDataStore.setMorningRatingEnabled(false)
                if (notificationId != -1) {
                    context.getSystemService(NotificationManager::class.java).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
