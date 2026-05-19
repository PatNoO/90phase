@file:Suppress("TooManyFunctions")

package com.example.a90phase.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.viewmodels.SettingsUiState
import com.example.a90phase.presentation.viewmodels.SettingsViewModel

private const val CYCLE_MIN = 60f
private const val CYCLE_MAX = 120f
private const val LATENCY_MIN = 5f
private const val LATENCY_MAX = 45f
private const val DISCOVERY_LOCK_THRESHOLD = 7
private const val APP_VERSION = "1.0.0-alpha"

private data class SettingsCallbacks(
    val onCycleLengthChanged: (Int) -> Unit,
    val onLatencyChanged: (Int) -> Unit,
    val onDailyCheckInToggled: (Boolean) -> Unit,
    val onShowCheckInPicker: () -> Unit,
    val onBedtimeReminderToggled: (Boolean) -> Unit,
    val onMorningRatingToggled: (Boolean) -> Unit,
    val onMorningBedtimeLogToggled: (Boolean) -> Unit,
    val onSmartWakeToggled: (Boolean) -> Unit,
    val onStartDiscovery: () -> Unit,
    val onCancelDiscovery: () -> Unit,
    val onShowDiscoveryInfo: () -> Unit,
    val onFirebaseSyncToggled: (Boolean) -> Unit,
    val onViewDiscoveryResults: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDiscoveryResults: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showDiscoveryInfoDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val callbacks = SettingsCallbacks(
        onCycleLengthChanged = viewModel::onCycleDurationChanged,
        onLatencyChanged = viewModel::onSleepLatencyChanged,
        onDailyCheckInToggled = viewModel::onDailyCheckInToggled,
        onShowCheckInPicker = { showCheckInPicker = true },
        onBedtimeReminderToggled = viewModel::onBedtimeReminderToggled,
        onMorningRatingToggled = viewModel::onMorningRatingToggled,
        onMorningBedtimeLogToggled = viewModel::onMorningBedtimeLogToggled,
        onSmartWakeToggled = viewModel::onSmartWakeToggled,
        onStartDiscovery = viewModel::onStartDiscoveryPhase,
        onCancelDiscovery = viewModel::onCancelDiscoveryPhase,
        onShowDiscoveryInfo = { showDiscoveryInfoDialog = true },
        onFirebaseSyncToggled = viewModel::onFirebaseSyncToggled,
        onViewDiscoveryResults = onNavigateToDiscoveryResults,
    )

    LaunchedEffect(Unit) {
        viewModel.errors.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
    ) {
        StarFieldBackground()
        if (state.isLoading) {
            CircularProgressIndicator(
                color = SleepColors.CyanGlow,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            Scaffold(
                topBar = { SettingsTopBar(onNavigateBack = onNavigateBack) },
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                SettingsContent(
                    state = state,
                    callbacks = callbacks,
                    modifier = Modifier.padding(padding),
                )
            }
        }
        SettingsDialogs(
            state = state,
            showCheckInPicker = showCheckInPicker,
            showDiscoveryInfo = showDiscoveryInfoDialog,
            onCheckInDismiss = { showCheckInPicker = false },
            onCheckInConfirm = { h, m -> viewModel.onReminderTimeChanged(h, m); showCheckInPicker = false },
            onDiscoveryInfoDismiss = { showDiscoveryInfoDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDialogs(
    state: SettingsUiState,
    showCheckInPicker: Boolean,
    showDiscoveryInfo: Boolean,
    onCheckInDismiss: () -> Unit,
    onCheckInConfirm: (Int, Int) -> Unit,
    onDiscoveryInfoDismiss: () -> Unit,
) {
    if (showCheckInPicker) {
        CheckInTimePickerDialog(
            initialHour = state.checkInHour,
            initialMinute = state.checkInMinute,
            onConfirm = onCheckInConfirm,
            onDismiss = onCheckInDismiss,
        )
    }
    if (showDiscoveryInfo) {
        DiscoveryPhaseInfoDialog(onDismiss = onDiscoveryInfoDismiss)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Navigate back"
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SleepColors.MidnightBlue,
        ),
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    callbacks: SettingsCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Medium),
    ) {
        item {
            SleepPreferencesSection(
                state = state,
                onCycleLengthChanged = callbacks.onCycleLengthChanged,
                onLatencyChanged = callbacks.onLatencyChanged,
            )
        }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item {
            NotificationsSection(
                state = state,
                onDailyCheckInToggled = callbacks.onDailyCheckInToggled,
                onShowCheckInPicker = callbacks.onShowCheckInPicker,
                onBedtimeReminderToggled = callbacks.onBedtimeReminderToggled,
                onMorningRatingToggled = callbacks.onMorningRatingToggled,
                onMorningBedtimeLogToggled = callbacks.onMorningBedtimeLogToggled,
            )
        }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item {
            FeaturesSection(
                state = state,
                onSmartWakeToggled = callbacks.onSmartWakeToggled,
                onStartDiscovery = callbacks.onStartDiscovery,
                onCancelDiscovery = callbacks.onCancelDiscovery,
                onShowDiscoveryInfo = callbacks.onShowDiscoveryInfo,
                onViewDiscoveryResults = callbacks.onViewDiscoveryResults,
            )
        }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item { DiscoveryProgressSection(state = state) }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item { DataPrivacySection(state = state, onFirebaseSyncToggled = callbacks.onFirebaseSyncToggled) }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item { AboutSection() }
        item { Spacer(modifier = Modifier.height(Spacing.XL)) }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
    ) {
        Text(
            text = title,
            style = SleepTypography.LabelMedium,
            color = SleepColors.Silver,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.XS),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .padding(Spacing.Medium),
            content = content,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = Spacing.XS),
        color = SleepColors.SlateBlue.copy(alpha = 0.3f),
    )
}

@Composable
private fun SettingsSliderRow(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = SleepTypography.BodyLarge, color = SleepColors.White)
            Text(text = valueLabel, style = SleepTypography.BodyLarge, color = SleepColors.CyanGlow)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range,
            modifier = Modifier.semantics { contentDescription = "$label slider, $valueLabel" },
            colors = SliderDefaults.colors(
                thumbColor = SleepColors.CyanGlow,
                activeTrackColor = SleepColors.CyanGlow,
                inactiveTrackColor = SleepColors.SlateBlue,
            ),
        )
    }
}

@Composable
private fun SleepPreferencesSection(
    state: SettingsUiState,
    onCycleLengthChanged: (Int) -> Unit,
    onLatencyChanged: (Int) -> Unit,
) {
    var cycleSlider by remember { mutableStateOf(state.cycleLengthMin.toFloat()) }
    var latencySlider by remember { mutableStateOf(state.sleepLatencyMin.toFloat()) }
    LaunchedEffect(state.cycleLengthMin) { cycleSlider = state.cycleLengthMin.toFloat() }
    LaunchedEffect(state.sleepLatencyMin) { latencySlider = state.sleepLatencyMin.toFloat() }

    SettingsSection(title = "SLEEP PREFERENCES") {
        SettingsSliderRow(
            label = "Cycle length",
            valueLabel = "${cycleSlider.toInt()} min",
            value = cycleSlider,
            range = CYCLE_MIN..CYCLE_MAX,
            onValueChange = { cycleSlider = it },
            onValueChangeFinished = { onCycleLengthChanged(cycleSlider.toInt()) },
        )
        SettingsDivider()
        SettingsSliderRow(
            label = "Sleep latency",
            valueLabel = "${latencySlider.toInt()} min",
            value = latencySlider,
            range = LATENCY_MIN..LATENCY_MAX,
            onValueChange = { latencySlider = it },
            onValueChangeFinished = { onLatencyChanged(latencySlider.toInt()) },
        )
    }
}

@Composable
private fun CheckInRow(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTapTime: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SleepToggle(
            label = "Daily Check-in",
            checked = enabled,
            onCheckedChange = onToggle,
        )
        if (enabled) {
            val timeText = "%02d:%02d".format(hour, minute)
            Text(
                text = "Notification at $timeText  →",
                style = SleepTypography.BodyMedium,
                color = SleepColors.CyanGlow,
                modifier = Modifier
                    .padding(start = Spacing.XS, top = Spacing.XXS, bottom = Spacing.XXS)
                    .minimumInteractiveComponentSize()
                    .clearAndSetSemantics {
                        contentDescription = "Check-in time $timeText, tap to change"
                        role = Role.Button
                    }
                    .clickable(onClick = onTapTime),
            )
        }
    }
}

@Composable
private fun NotificationsSection(
    state: SettingsUiState,
    onDailyCheckInToggled: (Boolean) -> Unit,
    onShowCheckInPicker: () -> Unit,
    onBedtimeReminderToggled: (Boolean) -> Unit,
    onMorningRatingToggled: (Boolean) -> Unit,
    onMorningBedtimeLogToggled: (Boolean) -> Unit,
) {
    SettingsSection(title = "NOTIFICATIONS") {
        CheckInRow(
            enabled = state.dailyCheckInEnabled,
            hour = state.checkInHour,
            minute = state.checkInMinute,
            onToggle = onDailyCheckInToggled,
            onTapTime = onShowCheckInPicker,
        )
        SettingsDivider()
        SleepToggle(
            label = "Bedtime Reminder",
            checked = state.bedtimeReminderEnabled,
            onCheckedChange = onBedtimeReminderToggled,
        )
        SettingsDivider()
        SleepToggle(
            label = "Morning Rating",
            checked = state.morningRatingEnabled,
            onCheckedChange = onMorningRatingToggled,
        )
        SettingsDivider()
        SleepToggle(
            label = "Morning Bedtime Log",
            checked = state.morningBedtimeLogEnabled,
            onCheckedChange = onMorningBedtimeLogToggled,
        )
    }
}

@Composable
private fun DiscoveryPhaseRow(
    state: SettingsUiState,
    onStartDiscovery: () -> Unit,
    onCancelDiscovery: () -> Unit,
    onShowInfo: () -> Unit,
    onViewDiscoveryResults: () -> Unit,
) {
    val locked = state.ratingDaysCount < DISCOVERY_LOCK_THRESHOLD
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Discovery Phase",
                    style = SleepTypography.BodyLarge,
                    color = if (locked && !state.discoveryPhaseCompleted) SleepColors.White.copy(alpha = 0.4f) else SleepColors.White,
                )
                if (locked && !state.discoveryPhaseCompleted) {
                    Text(
                        text = "  🔒",
                        style = SleepTypography.BodyLarge,
                        color = SleepColors.Silver.copy(alpha = 0.4f),
                    )
                }
            }
            Text(
                text = "ℹ",
                style = SleepTypography.BodyLarge,
                color = SleepColors.Silver,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clearAndSetSemantics { contentDescription = "Discovery Phase information" }
                    .clickable(onClick = onShowInfo),
            )
        }
        Spacer(modifier = Modifier.height(Spacing.XXS))
        DiscoveryPhaseStatus(
            state = state,
            locked = locked,
            onStartDiscovery = onStartDiscovery,
            onCancelDiscovery = onCancelDiscovery,
            onViewDiscoveryResults = onViewDiscoveryResults,
        )
        if (state.discoveryStartError != null) {
            Spacer(modifier = Modifier.height(Spacing.XXS))
            Text(
                text = state.discoveryStartError,
                style = SleepTypography.BodyMedium,
                color = SleepColors.ErrorRed,
            )
        }
    }
}

@Composable
private fun DiscoveryPhaseCompletedStatus(onViewDiscoveryResults: () -> Unit) {
    Text(text = "Phase complete", style = SleepTypography.BodyMedium, color = SleepColors.IndigoGlow)
    Spacer(modifier = Modifier.height(Spacing.XS))
    Text(
        text = "View Results →",
        style = SleepTypography.BodyMedium,
        color = SleepColors.CyanGlow,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clearAndSetSemantics { contentDescription = "View Discovery Phase results" }
            .clickable(onClick = onViewDiscoveryResults),
    )
}

@Composable
private fun DiscoveryPhaseStatus(
    state: SettingsUiState,
    locked: Boolean,
    onStartDiscovery: () -> Unit,
    onCancelDiscovery: () -> Unit,
    onViewDiscoveryResults: () -> Unit,
) {
    when {
        state.discoveryPhaseCompleted -> DiscoveryPhaseCompletedStatus(onViewDiscoveryResults)
        state.discoveryPhaseActive -> {
            Text(
                text = "Aktiv · Dag ${state.discoveryDayNumber}/21",
                style = SleepTypography.BodyMedium,
                color = SleepColors.IndigoGlow,
            )
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = "Avsluta →",
                style = SleepTypography.BodyMedium,
                color = SleepColors.ErrorRed,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clearAndSetSemantics { contentDescription = "Cancel Discovery Phase" }
                    .clickable(onClick = onCancelDiscovery),
            )
        }
        locked -> {
            Text(
                text = "${state.ratingDaysCount} / $DISCOVERY_LOCK_THRESHOLD days rated",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver.copy(alpha = 0.4f),
            )
        }
        else -> {
            Text(text = "Inaktiv", style = SleepTypography.BodyMedium, color = SleepColors.Silver)
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = "Starta Discovery Phase →",
                style = SleepTypography.BodyMedium,
                color = SleepColors.IndigoGlow,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clearAndSetSemantics { contentDescription = "Start Discovery Phase" }
                    .clickable(onClick = onStartDiscovery),
            )
        }
    }
}

@Composable
private fun DiscoveryPhaseInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Discovery Phase",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
                Text(
                    text = "Under 21 dagar testar appen tre varianter av din sömnrutin för att hitta vad som funkar bäst för dig.",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
                Text(
                    text = "• Vecka 1 — längre insomningstid (30 min)",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
                Text(
                    text = "• Vecka 2 — längre cykler (105 min)",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
                Text(
                    text = "• Vecka 3 — färre cykler (5 st)",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
                Text(
                    text = "Betygsätt din sömn varje dag. Efter 21 dagar väljs den bästa varianten automatiskt.",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Stäng", color = SleepColors.CyanGlow)
            }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Composable
private fun DiscoveryProgressSection(state: SettingsUiState) {
    AnimatedVisibility(visible = state.discoveryPhaseActive) {
        SettingsSection(title = "DISCOVERY PHASE PROGRESS") {
            DiscoveryProgressBar(dayNumber = state.discoveryDayNumber)
            Spacer(modifier = Modifier.height(Spacing.XS))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Day ${state.discoveryDayNumber} / 21",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.IndigoGlow,
                )
                Text(
                    text = "${state.discoveryWeekRatingsCount} ratings this week",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
            }
            if (state.discoveryCurrentShiftName.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.XXS))
                Text(
                    text = "Current shift: ${state.discoveryCurrentShiftName}",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
            }
        }
    }
}

@Composable
private fun DiscoveryProgressBar(dayNumber: Int) {
    val progress = (dayNumber.coerceIn(0, 21) / 21f)
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth(),
        color = SleepColors.IndigoGlow,
        trackColor = SleepColors.SlateBlue.copy(alpha = 0.3f),
    )
}

@Composable
private fun FeaturesSection(
    state: SettingsUiState,
    onSmartWakeToggled: (Boolean) -> Unit,
    onStartDiscovery: () -> Unit,
    onCancelDiscovery: () -> Unit,
    onShowDiscoveryInfo: () -> Unit,
    onViewDiscoveryResults: () -> Unit,
) {
    var patternInsightsEnabled by remember { mutableStateOf(false) }
    var consistencyScoreEnabled by remember { mutableStateOf(false) }
    SettingsSection(title = "FEATURES") {
        SleepToggle(
            label = "Smart Wake Window",
            checked = state.smartWakeEnabled,
            onCheckedChange = onSmartWakeToggled,
        )
        SettingsDivider()
        DiscoveryPhaseRow(
            state = state,
            onStartDiscovery = onStartDiscovery,
            onCancelDiscovery = onCancelDiscovery,
            onShowInfo = onShowDiscoveryInfo,
            onViewDiscoveryResults = onViewDiscoveryResults,
        )
        SettingsDivider()
        SleepToggle(
            label = "Pattern Insights in History",
            checked = patternInsightsEnabled,
            onCheckedChange = { patternInsightsEnabled = it },
        )
        SettingsDivider()
        SleepToggle(
            label = "Consistency Score in History",
            checked = consistencyScoreEnabled,
            onCheckedChange = { consistencyScoreEnabled = it },
        )
    }
}

@Composable
private fun SettingsLinkRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) { role = Role.Button }
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.XS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = SleepTypography.BodyLarge, color = SleepColors.White)
        Text(text = "→", style = SleepTypography.BodyLarge, color = SleepColors.Silver)
    }
}

@Composable
private fun DataPrivacySection(
    state: SettingsUiState,
    onFirebaseSyncToggled: (Boolean) -> Unit,
) {
    SettingsSection(title = "DATA & PRIVACY") {
        Column(modifier = Modifier.fillMaxWidth()) {
            SleepToggle(
                label = "Firebase Sync",
                checked = state.firebaseSyncEnabled,
                onCheckedChange = onFirebaseSyncToggled,
            )
            Text(
                text = "Last synced: Today",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
                modifier = Modifier.padding(start = Spacing.XS, bottom = Spacing.XS),
            )
        }
        SettingsDivider()
        SettingsLinkRow(label = "Export Data", onClick = {})
    }
}

@Composable
private fun AboutSection() {
    SettingsSection(title = "ABOUT") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Version", style = SleepTypography.BodyLarge, color = SleepColors.White)
            Text(text = APP_VERSION, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
        }
        SettingsDivider()
        SettingsLinkRow(label = "Privacy Policy", onClick = {})
        SettingsDivider()
        SettingsLinkRow(label = "GitHub", onClick = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Check-in Time",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
                Text(text = "Confirm", color = SleepColors.CyanGlow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = SleepColors.Silver)
            }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SettingsScreenPreview() {
    NightSkyTheme {
        SettingsScreen(onNavigateBack = {}, onNavigateToDiscoveryResults = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun DiscoveryProgressSectionPreview() {
    NightSkyTheme {
        DiscoveryProgressSection(
            state = SettingsUiState(
                isLoading = false,
                discoveryPhaseActive = true,
                discoveryDayNumber = 9,
                discoveryCurrentShiftName = "Longer cycles (105 min)",
                discoveryWeekRatingsCount = 2,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120, widthDp = 640, heightDp = 360)
@Composable
internal fun SettingsScreenLandscapePreview() {
    NightSkyTheme {
        SettingsScreen(onNavigateBack = {}, onNavigateToDiscoveryResults = {})
    }
}
