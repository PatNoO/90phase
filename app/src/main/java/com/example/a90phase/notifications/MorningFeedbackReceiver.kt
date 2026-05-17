package com.example.a90phase.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.a90phase.R
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MorningFeedbackReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val enabled = userPreferencesDataStore.observeMorningRatingEnabled().first()
                if (!enabled) return@launch
                postNotification(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val remoteViews = buildRemoteViews(context)

        val notification = NotificationCompat.Builder(context, NotificationChannels.MORNING_FEEDBACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_morning_title))
            .setContentText(context.getString(R.string.notification_morning_body))
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.notification_morning_feedback)
        views.setTextViewText(R.id.notification_body, context.getString(R.string.notification_morning_body))

        val starViewIds = listOf(R.id.btn_star_1, R.id.btn_star_2, R.id.btn_star_3, R.id.btn_star_4, R.id.btn_star_5)
        starViewIds.forEachIndexed { index, viewId ->
            val rating = index + 1
            val intent = Intent(context, RatingActionReceiver::class.java).apply {
                putExtra(RatingActionReceiver.EXTRA_RATING, rating)
                putExtra(RatingActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                STAR_REQUEST_BASE + rating,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(viewId, pending)
        }

        val skipIntent = Intent(context, SkipMorningFeedbackReceiver::class.java).apply {
            putExtra(SkipMorningFeedbackReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        }
        views.setOnClickPendingIntent(
            R.id.btn_skip,
            PendingIntent.getBroadcast(
                context,
                SKIP_REQUEST_CODE,
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

        val disableIntent = Intent(context, DisableMorningFeedbackReceiver::class.java).apply {
            putExtra(DisableMorningFeedbackReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
        }
        views.setOnClickPendingIntent(
            R.id.tv_turn_off,
            PendingIntent.getBroadcast(
                context,
                DISABLE_REQUEST_CODE,
                disableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

        return views
    }

    companion object {
        const val NOTIFICATION_ID = 4001
        private const val STAR_REQUEST_BASE = 4010
        private const val SKIP_REQUEST_CODE = 4020
        private const val DISABLE_REQUEST_CODE = 4021
    }
}
