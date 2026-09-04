package com.example.a90phase.presentation.screens.calculator

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.BedtimeRecommendation
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.components.BedtimeResultCard
import com.example.a90phase.presentation.components.SecondaryButton
import com.example.a90phase.presentation.components.SectionHeader
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.components.WakeTimeCard
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.viewmodels.CalculatorViewModel
import com.example.a90phase.presentation.viewmodels.SleepCalculatorUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class CalculatorScreenState(
    val wakeTime: LocalTime,
    val isWakeTimeActive: Boolean,
    val isAlarmActive: Boolean,
    val isDailyReminderActive: Boolean,
    val selectedBedtimeIndex: Int,
    val bedtimes: List<BedtimeRecommendation>,
    val isSaving: Boolean = false,
)

@Composable
private fun BedtimeRecommendation.toDurationLabel(): String =
    if (quality == BedtimeQuality.PASSED) {
        stringResource(R.string.calculator_duration_passed)
    } else {
        stringResource(R.string.calculator_duration_sleep, durationMinutes / 60, durationMinutes % 60)
    }

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    var isWakeTimeActive by rememberSaveable { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val vmState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bedtimeSavedMessage = stringResource(R.string.calculator_bedtime_saved)
    val saveFailedMessage = stringResource(R.string.calculator_save_failed)

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { success ->
            scope.launch {
                snackbarHostState.showSnackbar(if (success) bedtimeSavedMessage else saveFailedMessage)
            }
        }
    }

    when (val state = vmState) {
        is SleepCalculatorUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                StarFieldBackground()
                CircularProgressIndicator(color = SleepColors.CyanGlow)
            }
        }
        is SleepCalculatorUiState.Error -> CalculatorErrorState(
            message = stringResource(state.messageRes),
            onRetry = viewModel::retry,
        )
        is SleepCalculatorUiState.Success -> {
            CalculatorScaffold(
                state = CalculatorScreenState(
                    wakeTime = state.wakeTime,
                    isWakeTimeActive = isWakeTimeActive,
                    isAlarmActive = state.alarmActive,
                    isDailyReminderActive = state.dailyCheckInEnabled,
                    selectedBedtimeIndex = state.selectedBedtimeIndex,
                    bedtimes = state.bedtimes,
                    isSaving = state.isSaving,
                ),
                snackbarHostState = snackbarHostState,
                onNavigateToSettings = onNavigateToSettings,
                onWakeTimeClick = { showTimePicker = true },
                onAlarmToggle = { viewModel.onAlarmActiveToggled(it) },
                onReminderToggle = { viewModel.onDailyCheckInToggled(it) },
                onBedtimeSelect = { index ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onBedtimeSelected(state.bedtimes[index], index)
                },
                onSaveClicked = viewModel::onSaveClicked,
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
            com.example.a90phase.presentation.components.PrimaryButton(
                text = stringResource(R.string.calculator_try_again),
                onClick = onRetry,
            )
        }
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorScaffold(
    state: CalculatorScreenState,
    snackbarHostState: SnackbarHostState,
    onNavigateToSettings: () -> Unit,
    onWakeTimeClick: () -> Unit,
    onAlarmToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onBedtimeSelect: (Int) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        StarFieldBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { CalculatorTopBar(onNavigateToSettings = onNavigateToSettings) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = Spacing.XL),
            ) {
                item {
                    PermissionWarningBanner()
                }
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
                    AlarmSuggestionBanner(wakeTime = state.wakeTime, isActive = state.isAlarmActive)
                }
                item {
                    Spacer(modifier = Modifier.height(Spacing.Large))
                    SectionHeader(title = stringResource(R.string.calculator_recommended_bedtimes))
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
                if (state.selectedBedtimeIndex >= 0) {
                    item {
                        Spacer(modifier = Modifier.height(Spacing.Medium))
                        SaveBedtimeButton(isSaving = state.isSaving, onClick = onSaveClicked)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaveBedtimeButton(isSaving: Boolean, onClick: () -> Unit) {
    com.example.a90phase.presentation.components.PrimaryButton(
        text = stringResource(if (isSaving) R.string.calculator_saving else R.string.calculator_save),
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
    )
}

@Composable
private fun AlarmSuggestionBanner(wakeTime: LocalTime, isActive: Boolean) {
    // Only shown when the app's own wake alarm is active; reflects the wake time.
    if (!isActive) return
    val formatted = wakeTime.format(DateTimeFormatter.ofPattern("HH:mm"))
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
            Text(
                text = stringResource(R.string.calculator_alarm_banner, formatted),
                style = SleepTypography.BodyMedium,
                color = SleepColors.CyanGlow,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorTopBar(onNavigateToSettings: () -> Unit) {
    val openSettingsDescription = stringResource(R.string.calculator_open_settings)
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.calculator_title),
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
                        contentDescription = openSettingsDescription
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
        SleepToggle(
            label = stringResource(R.string.calculator_alarm_active),
            checked = isAlarmActive,
            onCheckedChange = onAlarmToggle,
        )
        SleepToggle(
            label = stringResource(R.string.calculator_daily_checkin_toggle),
            checked = isDailyReminderActive,
            onCheckedChange = onReminderToggle,
        )
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
        title = {
            Text(
                text = stringResource(R.string.calculator_select_wake_time),
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(LocalTime.of(state.hour, state.minute)) }) {
                Text(text = stringResource(R.string.common_ok), color = SleepColors.CyanGlow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.common_cancel), color = SleepColors.Silver) }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

/**
 * What the app cannot do right now because Android is withholding a permission.
 *
 * Both are granted during onboarding and never re-checked, so a user who declines — or revokes
 * later — gets an app that silently stops notifying with nothing on screen to explain why.
 */
private enum class PermissionWarning { NotificationsBlocked, ExactAlarmsBlocked }

/**
 * Re-reads the permissions on every resume, so returning from system settings updates the banner
 * immediately rather than on the next cold start.
 */
@Composable
private fun rememberPermissionWarning(): PermissionWarning? {
    val context = LocalContext.current
    var warning by remember { mutableStateOf(currentPermissionWarning(context)) }
    LifecycleResumeEffect(Unit) {
        warning = currentPermissionWarning(context)
        onPauseOrDispose { }
    }
    return warning
}

private fun currentPermissionWarning(context: Context): PermissionWarning? {
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    if (!notificationManager.areNotificationsEnabled()) return PermissionWarning.NotificationsBlocked
    // canScheduleExactAlarms is API 31; below that exact alarms are always allowed.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) return PermissionWarning.ExactAlarmsBlocked
    }
    return null
}

private fun openPermissionSettings(context: Context, warning: PermissionWarning) {
    val intent = when (warning) {
        PermissionWarning.NotificationsBlocked ->
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        PermissionWarning.ExactAlarmsBlocked ->
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    }
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
private fun PermissionWarningBanner() {
    val warning = rememberPermissionWarning() ?: return
    val context = LocalContext.current
    val title = when (warning) {
        PermissionWarning.NotificationsBlocked -> R.string.permission_banner_notifications_title
        PermissionWarning.ExactAlarmsBlocked -> R.string.permission_banner_exact_alarm_title
    }
    val body = when (warning) {
        PermissionWarning.NotificationsBlocked -> R.string.permission_banner_notifications_body
        PermissionWarning.ExactAlarmsBlocked -> R.string.permission_banner_exact_alarm_body
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
            .padding(top = Spacing.Medium)
            .glassCard()
            .padding(Spacing.Medium),
    ) {
        Text(
            text = stringResource(title),
            style = SleepTypography.BodyLarge,
            color = SleepColors.GoodAmber,
        )
        Spacer(modifier = Modifier.height(Spacing.XXS))
        Text(
            text = stringResource(body),
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        SecondaryButton(
            text = stringResource(R.string.permission_banner_action),
            onClick = { openPermissionSettings(context, warning) },
        )
    }
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
