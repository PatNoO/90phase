package com.example.a90phase.presentation.screens.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.presentation.components.BedtimeResultCard
import com.example.a90phase.presentation.components.SectionHeader
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.components.WakeTimeCard
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.viewmodels.CalculatorViewModel
import com.example.a90phase.presentation.viewmodels.SleepCalculatorUiState
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class CalculatorScreenState(
    val wakeTime: LocalTime,
    val isWakeTimeActive: Boolean,
    val isAlarmActive: Boolean,
    val isDailyReminderActive: Boolean,
    val selectedBedtimeIndex: Int,
    val bedtimes: List<BedtimeRecommendation>,
    val nextSystemAlarm: SystemAlarm? = null,
)

private fun BedtimeRecommendation.toDurationLabel(): String =
    if (quality == BedtimeQuality.PASSED) "Passed" else "${durationMinutes / 60}h ${durationMinutes % 60}min sleep"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    var isWakeTimeActive by rememberSaveable { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isAlarmActive by rememberSaveable { mutableStateOf(false) }
    var isDailyReminderActive by rememberSaveable { mutableStateOf(true) }
    val hapticFeedback = LocalHapticFeedback.current
    val vmState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = vmState) {
        is SleepCalculatorUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                StarFieldBackground()
                CircularProgressIndicator(color = SleepColors.CyanGlow)
            }
        }
        is SleepCalculatorUiState.Error -> CalculatorErrorState(
            message = state.message,
            onRetry = viewModel::retry,
        )
        is SleepCalculatorUiState.Success -> {
            CalculatorScaffold(
                state = CalculatorScreenState(
                    wakeTime = state.wakeTime,
                    isWakeTimeActive = isWakeTimeActive,
                    isAlarmActive = isAlarmActive,
                    isDailyReminderActive = isDailyReminderActive,
                    selectedBedtimeIndex = state.selectedBedtimeIndex,
                    bedtimes = state.bedtimes,
                    nextSystemAlarm = state.nextSystemAlarm,
                ),
                onNavigateToSettings = onNavigateToSettings,
                onWakeTimeClick = { showTimePicker = true },
                onAlarmToggle = { isAlarmActive = it },
                onReminderToggle = { isDailyReminderActive = it },
                onBedtimeSelect = { index ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onBedtimeSelected(state.bedtimes[index], index)
                },
            )
            if (showTimePicker) {
                WakeTimePickerDialog(
                    initialTime = state.wakeTime,
                    onTimeSelected = { selected ->
                        isWakeTimeActive = true
                        showTimePicker = false
                        viewModel.onWakeTimeChanged(selected)
                    },
                    onDismiss = { showTimePicker = false },
                )
            }
        }
    }
}

@Composable
private fun CalculatorErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StarFieldBackground()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Spacing.Large),
        ) {
            Text(
                text = message,
                color = SleepColors.ErrorRed,
                style = SleepTypography.BodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Spacing.Medium))
            com.example.a90phase.presentation.components.PrimaryButton(text = "Försök igen", onClick = onRetry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorScaffold(
    state: CalculatorScreenState,
    onNavigateToSettings: () -> Unit,
    onWakeTimeClick: () -> Unit,
    onAlarmToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onBedtimeSelect: (Int) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        StarFieldBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { CalculatorTopBar(onNavigateToSettings = onNavigateToSettings) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = Spacing.XL),
            ) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    WakeTimeCard(time = state.wakeTime, isActive = state.isWakeTimeActive, onClick = onWakeTimeClick)
                }
                item {
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    CalculatorToggles(
                        isAlarmActive = state.isAlarmActive,
                        isDailyReminderActive = state.isDailyReminderActive,
                        onAlarmToggle = onAlarmToggle,
                        onReminderToggle = onReminderToggle,
                    )
                }
                item {
                    AlarmSuggestionBanner(alarm = state.nextSystemAlarm)
                }
                item {
                    Spacer(modifier = Modifier.height(Spacing.Large))
                    SectionHeader(title = "Recommended Bedtimes")
                    Spacer(modifier = Modifier.height(Spacing.XS))
                }
                itemsIndexed(state.bedtimes) { index, bedtime ->
                    StaggeredBedtimeCard(
                        index = index,
                        bedtime = bedtime,
                        isSelected = state.selectedBedtimeIndex == index,
                        onSelect = { onBedtimeSelect(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmSuggestionBanner(alarm: SystemAlarm?) {
    if (alarm == null) return
    val localTime = alarm.time.atZone(ZoneId.systemDefault()).toLocalTime()
    val formatted = localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    Spacer(modifier = Modifier.height(Spacing.Medium))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = SleepColors.NavyBlue.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "⏰", style = SleepTypography.HeadlineMedium)
            Spacer(modifier = Modifier.width(Spacing.Small))
            Column {
                Text(
                    text = "Din alarm: $formatted",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.CyanGlow,
                )
                val label = alarm.label
                if (!label.isNullOrBlank()) {
                    Text(
                        text = label,
                        style = SleepTypography.LabelMedium,
                        color = SleepColors.Silver,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorTopBar(onNavigateToSettings: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Sleep Cycle Optimizer",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        actions = {
            IconButton(onClick = onNavigateToSettings) {
                Text(
                    text = "⚙",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Open settings"
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = SleepColors.NavyBlue.copy(alpha = 0.8f),
        ),
    )
}

@Composable
private fun CalculatorToggles(
    isAlarmActive: Boolean,
    isDailyReminderActive: Boolean,
    onAlarmToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.Medium)) {
        SleepToggle(label = "Alarm Active", checked = isAlarmActive, onCheckedChange = onAlarmToggle)
        SleepToggle(label = "Daily Check-in (18:00)", checked = isDailyReminderActive, onCheckedChange = onReminderToggle)
    }
}

@Composable
private fun StaggeredBedtimeCard(
    index: Int,
    bedtime: BedtimeRecommendation,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80L * index)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
            slideInVertically(animationSpec = tween(300), initialOffsetY = { it / 4 }),
    ) {
        BedtimeResultCard(
            time = bedtime.bedtime,
            cycleCount = bedtime.cycleCount,
            durationLabel = bedtime.toDurationLabel(),
            quality = bedtime.quality,
            isSelected = isSelected,
            onClick = onSelect,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WakeTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Wake Time", style = SleepTypography.HeadlineMedium, color = SleepColors.White) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(LocalTime.of(state.hour, state.minute)) }) {
                Text(text = "OK", color = SleepColors.CyanGlow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel", color = SleepColors.Silver) }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun CalculatorScreenPreview() {
    NightSkyTheme {
        CalculatorScreen(onNavigateToSettings = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120, widthDp = 640, heightDp = 360)
@Composable
internal fun CalculatorScreenLandscapePreview() {
    NightSkyTheme {
        CalculatorScreen(onNavigateToSettings = {})
    }
}
