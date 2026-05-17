package com.example.a90phase

import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.a90phase.domain.common.DomainConstants
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.AnalyzeDiscoveryPhaseUseCase
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.presentation.theme.NightSkyTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MorningBedtimeLogActivity : ComponentActivity() {

    @Inject lateinit var sleepRepository: SleepRepository
    @Inject lateinit var userPreferencesDataStore: UserPreferencesDataStore
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rating = intent.getIntExtra(EXTRA_RATING, -1)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (rating == -1) {
            finish()
            return
        }
        setContent {
            NightSkyTheme {
                BedtimeLogSheet(
                    rating = rating,
                    notificationId = notificationId,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @androidx.compose.runtime.Composable
    private fun BedtimeLogSheet(rating: Int, notificationId: Int) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val timePickerState = rememberTimePickerState(initialHour = 23, initialMinute = 0, is24Hour = true)
        var showSheet by remember { mutableStateOf(true) }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    saveLog(rating, bedtimeHour = null, bedtimeMinute = null, notificationId)
                },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = getString(R.string.morning_sheet_title))
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = {
                            showSheet = false
                            saveLog(rating, bedtimeHour = null, bedtimeMinute = null, notificationId)
                        }) { Text(text = getString(R.string.morning_sheet_skip)) }
                        Button(onClick = {
                            showSheet = false
                            saveLog(rating, timePickerState.hour, timePickerState.minute, notificationId)
                        }) { Text(text = getString(R.string.morning_sheet_save)) }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            finish()
        }
    }

    private fun saveLog(rating: Int, bedtimeHour: Int?, bedtimeMinute: Int?, notificationId: Int) {
        lifecycleScope.launch {
            val profileResult = userPreferencesRepository.getUserProfile()
            val profile = (profileResult as? Result.Success)?.data

            val wakeHour = userPreferencesDataStore.observeSelectedWakeHour().first()
            val wakeMinute = userPreferencesDataStore.observeSelectedWakeMinute().first()
            val cycleCount = userPreferencesDataStore.observeSelectedBedtimeCycles().first()
            val today = LocalDate.now()
            val wakeInstant = today
                .atTime(wakeHour, wakeMinute)
                .atZone(ZoneId.systemDefault())
                .toInstant()
            val bedtimeInstant = if (bedtimeHour != null && bedtimeMinute != null) {
                val bedtimeDate = if (bedtimeHour >= wakeHour) today.minusDays(1) else today
                bedtimeDate
                    .atTime(LocalTime.of(bedtimeHour, bedtimeMinute))
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            } else {
                null
            }

            val log = SleepLog(
                id = UUID.randomUUID().toString(),
                date = today,
                wakeTime = wakeInstant,
                bedtime = bedtimeInstant,
                qualityRating = rating,
                cycleCount = cycleCount,
                cycleDurationUsed = profile?.optimalCycleMinutes ?: DomainConstants.CYCLE_DURATION_MINUTES,
                sleepLatencyUsed = profile?.sleepLatencyMinutes ?: DomainConstants.SLEEP_LATENCY_MINUTES,
                syncStatus = SyncStatus.PENDING_UPLOAD,
            )
            sleepRepository.saveSleepLog(log)

            if (notificationId != -1) {
                getSystemService(NotificationManager::class.java).cancel(notificationId)
            }

            if (profile?.isDiscoveryPhaseActive() == true) {
                AnalyzeDiscoveryPhaseUseCase(userPreferencesRepository).invoke()
            }

            finish()
        }
    }

    companion object {
        const val EXTRA_RATING = "extra_rating"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
