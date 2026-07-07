package com.example.a90phase

import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.a90phase.notifications.WakeAlarmReceiver
import com.example.a90phase.notifications.WakeAlarmScheduler
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Full-screen ringing screen shown over the lock screen when the wake alarm fires.
 * Plays a looping alarm sound + vibration and offers Dismiss and Snooze.
 */
@AndroidEntryPoint
class AlarmRingActivity : ComponentActivity() {

    @Inject lateinit var wakeAlarmScheduler: WakeAlarmScheduler
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()
        startRinging()
        setContent {
            NightSkyTheme {
                AlarmRingScreen(
                    timeText = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    onDismiss = ::onDismiss,
                    onSnooze = ::onSnooze,
                )
            }
        }
    }

    private fun onDismiss() {
        ioScope.launch { userPreferencesDataStore.setWakeAlarmEnabled(false) }
        stopAndFinish()
    }

    private fun onSnooze() {
        wakeAlarmScheduler.snooze(SNOOZE_MINUTES)
        stopAndFinish()
    }

    private fun stopAndFinish() {
        stopRinging()
        getSystemService(NotificationManager::class.java).cancel(WakeAlarmReceiver.NOTIFICATION_ID)
        finish()
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        )
    }

    private fun startRinging() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@AlarmRingActivity, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            isLooping = true
            prepare()
            start()
        }
        startVibration()
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VibratorManager::class.java)).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        val pattern = longArrayOf(0, 800, 1000)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopRinging() {
        mediaPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    companion object {
        private const val SNOOZE_MINUTES = 9L
    }
}

@Composable
private fun AlarmRingScreen(
    timeText: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = timeText, style = SleepTypography.DisplayLarge, color = SleepColors.White)
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(text = "Dags att vakna", style = SleepTypography.HeadlineMedium, color = SleepColors.Silver)
        Spacer(modifier = Modifier.height(Spacing.XL))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Stäng")
        }
        Spacer(modifier = Modifier.height(Spacing.Medium))
        OutlinedButton(
            onClick = onSnooze,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Snooza 9 min")
        }
    }
}
