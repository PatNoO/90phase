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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.data.local.datastore.SelectedBedtimePlan
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.notifications.WakeAlarmReceiver
import com.example.a90phase.notifications.WakeAlarmScheduler
import com.example.a90phase.presentation.components.PrimaryButton
import com.example.a90phase.presentation.components.SecondaryButton
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.util.FULL_DATE_PATTERN
import com.example.a90phase.presentation.util.formatSleepDuration
import com.example.a90phase.presentation.util.rememberDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()
        startRinging()
        setContent {
            NightSkyTheme {
                AlarmRingScreen(
                    planFlow = userPreferencesDataStore.observeSelectedBedtimePlan(),
                    snoozeMinutes = SNOOZE_MINUTES,
                    onDismiss = ::onDismiss,
                    onSnooze = ::onSnooze,
                )
            }
        }
    }

    /**
     * Ends this morning's alarm only — the alarm stays switched on and rings again tomorrow,
     * the way a phone's own alarm behaves. Tomorrow's alarm was already re-armed by
     * [WakeAlarmReceiver] when it fired, so all that is left here is to stop the noise and
     * drop any snooze the user had queued.
     */
    private fun onDismiss() {
        wakeAlarmScheduler.cancelSnooze()
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

private const val CLOCK_PATTERN = "HH:mm"
private const val MILLIS_PER_MINUTE = 60_000L

/**
 * The wall clock, re-read on every minute boundary so a long-ringing alarm never shows a
 * stale time.
 */
@Composable
private fun rememberTickingTime(): State<LocalTime> {
    val time = remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            time.value = LocalTime.now()
            delay(MILLIS_PER_MINUTE - System.currentTimeMillis() % MILLIS_PER_MINUTE)
        }
    }
    return time
}

@Composable
private fun AlarmRingScreen(
    planFlow: Flow<SelectedBedtimePlan?>,
    snoozeMinutes: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    val now by rememberTickingTime()
    val plan by planFlow.collectAsState(initial = null)
    AlarmRingContent(
        time = now,
        date = LocalDate.now(),
        plan = plan,
        snoozeMinutes = snoozeMinutes,
        onDismiss = onDismiss,
        onSnooze = onSnooze,
    )
}

@Composable
private fun AlarmRingContent(
    time: LocalTime,
    date: LocalDate,
    plan: SelectedBedtimePlan?,
    snoozeMinutes: Long,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
    ) {
        StarFieldBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = date.format(rememberDateFormatter(FULL_DATE_PATTERN)),
                style = SleepTypography.LabelMedium,
                color = SleepColors.Silver,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
            Text(
                text = time.format(DateTimeFormatter.ofPattern(CLOCK_PATTERN)),
                style = SleepTypography.DisplayLarge,
                color = SleepColors.White,
            )
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = stringResource(R.string.alarm_ring_message),
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.Silver,
                textAlign = TextAlign.Center,
            )
            if (plan != null) {
                Spacer(modifier = Modifier.height(Spacing.Large))
                SleepPlanSummary(plan = plan)
            }
            Spacer(modifier = Modifier.height(Spacing.XL))
            PrimaryButton(
                text = stringResource(R.string.alarm_ring_dismiss),
                onClick = onDismiss,
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))
            SecondaryButton(
                text = stringResource(R.string.alarm_ring_snooze, snoozeMinutes.toInt()),
                onClick = onSnooze,
            )
        }
    }
}

@Composable
private fun SleepPlanSummary(plan: SelectedBedtimePlan) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard()
            .padding(Spacing.Medium),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.alarm_ring_plan_label),
                style = SleepTypography.LabelMedium,
                color = SleepColors.Silver,
            )
            Spacer(modifier = Modifier.height(Spacing.XXS))
            Text(
                text = stringResource(
                    R.string.alarm_ring_plan_value,
                    pluralStringResource(R.plurals.alarm_ring_cycles, plan.cycleCount, plan.cycleCount),
                    formatSleepDuration(plan.durationMinutes),
                ),
                style = SleepTypography.BodyLarge,
                color = SleepColors.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun AlarmRingContentPreview() {
    NightSkyTheme {
        AlarmRingContent(
            time = LocalTime.of(7, 0),
            date = LocalDate.of(2025, 5, 15),
            plan = SelectedBedtimePlan(cycleCount = 6, durationMinutes = 555),
            snoozeMinutes = 9L,
            onDismiss = {},
            onSnooze = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun AlarmRingContentNoPlanPreview() {
    NightSkyTheme {
        AlarmRingContent(
            time = LocalTime.of(5, 30),
            date = LocalDate.of(2025, 5, 15),
            plan = null,
            snoozeMinutes = 9L,
            onDismiss = {},
            onSnooze = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun AlarmRingScreenPreview() {
    NightSkyTheme {
        AlarmRingScreen(
            planFlow = flowOf(SelectedBedtimePlan(cycleCount = 5, durationMinutes = 465)),
            snoozeMinutes = 9L,
            onDismiss = {},
            onSnooze = {},
        )
    }
}
