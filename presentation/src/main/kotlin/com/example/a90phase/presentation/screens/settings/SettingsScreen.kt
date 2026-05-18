@file:Suppress("ForbiddenComment")

package com.example.a90phase.presentation.screens.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard

private const val CYCLE_MIN = 60f
private const val CYCLE_MAX = 120f
private const val CYCLE_DEFAULT = 90
private const val LATENCY_MIN = 5f
private const val LATENCY_MAX = 45f
private const val LATENCY_DEFAULT = 15
private const val DISCOVERY_LOCK_THRESHOLD = 7
private const val FAKE_RATING_DAYS = 3
private const val APP_VERSION = "1.0.0-alpha"

private data class SettingsUiState(
    val cycleLengthMin: Int = CYCLE_DEFAULT,
    val sleepLatencyMin: Int = LATENCY_DEFAULT,
    val dailyCheckInEnabled: Boolean = true,
    val checkInHour: Int = 18,
    val checkInMinute: Int = 0,
    val bedtimeReminderEnabled: Boolean = true,
    val morningRatingEnabled: Boolean = false,
    val morningBedtimeLogEnabled: Boolean = false,
    val smartWakeEnabled: Boolean = true,
    val patternInsightsEnabled: Boolean = false,
    val consistencyScoreEnabled: Boolean = false,
    val firebaseSyncEnabled: Boolean = true,
    val ratingDaysCount: Int = FAKE_RATING_DAYS,
    val discoveryPhaseActive: Boolean = false,
    val discoveryDayNumber: Int = 0,
    val discoveryStartError: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    // TODO: wire to ViewModel
    var state by remember { mutableStateOf(SettingsUiState()) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showDiscoveryInfoDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
    ) {
        StarFieldBackground()
        Scaffold(
            topBar = { SettingsTopBar(onNavigateBack = onNavigateBack) },
            containerColor = Color.Transparent,
        ) { padding ->
            SettingsContent(
                state = state,
                onStateChange = { state = it },
                onShowCheckInPicker = { showCheckInPicker = true },
                onShowDiscoveryInfo = { showDiscoveryInfoDialog = true },
                modifier = Modifier.padding(padding),
            )
        }
        if (showCheckInPicker) {
            CheckInTimePickerDialog(
                initialHour = state.checkInHour,
                initialMinute = state.checkInMinute,
                onConfirm = { h, m ->
                    state = state.copy(checkInHour = h, checkInMinute = m)
                    showCheckInPicker = false
                },
                onDismiss = { showCheckInPicker = false },
            )
        }
        if (showDiscoveryInfoDialog) {
            DiscoveryPhaseInfoDialog(onDismiss = { showDiscoveryInfoDialog = false })
        }
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
    onStateChange: (SettingsUiState) -> Unit,
    onShowCheckInPicker: () -> Unit,
    onShowDiscoveryInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Medium),
    ) {
        item { SleepPreferencesSection(state = state, onStateChange = onStateChange) }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item {
            NotificationsSection(
                state = state,
                onStateChange = onStateChange,
                onShowCheckInPicker = onShowCheckInPicker,
            )
        }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item {
            FeaturesSection(
                state = state,
                onStateChange = onStateChange,
                onShowDiscoveryInfo = onShowDiscoveryInfo,
            )
        }
        item { Spacer(modifier = Modifier.height(Spacing.Medium)) }
        item { DataPrivacySection(state = state, onStateChange = onStateChange) }
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
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = SleepTypography.BodyLarge,
                color = SleepColors.White,
            )
            Text(
                text = valueLabel,
                style = SleepTypography.BodyLarge,
                color = SleepColors.CyanGlow,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
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
    onStateChange: (SettingsUiState) -> Unit,
) {
    SettingsSection(title = "SLEEP PREFERENCES") {
        SettingsSliderRow(
            label = "Cycle length",
            valueLabel = "${state.cycleLengthMin} min",
            value = state.cycleLengthMin.toFloat(),
            range = CYCLE_MIN..CYCLE_MAX,
            onValueChange = { onStateChange(state.copy(cycleLengthMin = it.toInt())) },
        )
        SettingsDivider()
        SettingsSliderRow(
            label = "Sleep latency",
            valueLabel = "${state.sleepLatencyMin} min",
            value = state.sleepLatencyMin.toFloat(),
            range = LATENCY_MIN..LATENCY_MAX,
            onValueChange = { onStateChange(state.copy(sleepLatencyMin = it.toInt())) },
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
    onStateChange: (SettingsUiState) -> Unit,
    onShowCheckInPicker: () -> Unit,
) {
    SettingsSection(title = "NOTIFICATIONS") {
        CheckInRow(
            enabled = state.dailyCheckInEnabled,
            hour = state.checkInHour,
            minute = state.checkInMinute,
            onToggle = { onStateChange(state.copy(dailyCheckInEnabled = it)) },
            onTapTime = onShowCheckInPicker,
        )
        SettingsDivider()
        SleepToggle(
            label = "Bedtime Reminder",
            checked = state.bedtimeReminderEnabled,
            onCheckedChange = { onStateChange(state.copy(bedtimeReminderEnabled = it)) },
        )
        SettingsDivider()
        SleepToggle(
            label = "Morning Rating",
            checked = state.morningRatingEnabled,
            onCheckedChange = { onStateChange(state.copy(morningRatingEnabled = it)) },
        )
        SettingsDivider()
        SleepToggle(
            label = "Morning Bedtime Log",
            checked = state.morningBedtimeLogEnabled,
            onCheckedChange = { onStateChange(state.copy(morningBedtimeLogEnabled = it)) },
        )
    }
}

@Composable
private fun DiscoveryPhaseRow(
    state: SettingsUiState,
    onStartDiscovery: () -> Unit,
    onCancelDiscovery: () -> Unit,
    onShowInfo: () -> Unit,
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
                    color = if (locked) SleepColors.White.copy(alpha = 0.4f) else SleepColors.White,
                )
                if (locked) {
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
private fun DiscoveryPhaseStatus(
    state: SettingsUiState,
    locked: Boolean,
    onStartDiscovery: () -> Unit,
    onCancelDiscovery: () -> Unit,
) {
    when {
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
                    .clickable(onClick = onCancelDiscovery), // TODO: wire to ViewModel
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
            Text(
                text = "Inaktiv",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
            Spacer(modifier = Modifier.height(Spacing.XS))
            Text(
                text = "Starta Discovery Phase →",
                style = SleepTypography.BodyMedium,
                color = SleepColors.IndigoGlow,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clearAndSetSemantics { contentDescription = "Start Discovery Phase" }
                    .clickable(onClick = onStartDiscovery), // TODO: wire to ViewModel
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
private fun FeaturesSection(
    state: SettingsUiState,
    onStateChange: (SettingsUiState) -> Unit,
    onShowDiscoveryInfo: () -> Unit,
) {
    SettingsSection(title = "FEATURES") {
        SleepToggle(
            label = "Smart Wake Window",
            checked = state.smartWakeEnabled,
            onCheckedChange = { onStateChange(state.copy(smartWakeEnabled = it)) },
        )
        SettingsDivider()
        DiscoveryPhaseRow(
            state = state,
            onStartDiscovery = { /* TODO: wire to ViewModel */ },
            onCancelDiscovery = { /* TODO: wire to ViewModel */ },
            onShowInfo = onShowDiscoveryInfo,
        )
        SettingsDivider()
        SleepToggle(
            label = "Pattern Insights in History",
            checked = state.patternInsightsEnabled,
            onCheckedChange = { onStateChange(state.copy(patternInsightsEnabled = it)) },
        )
        SettingsDivider()
        SleepToggle(
            label = "Consistency Score in History",
            checked = state.consistencyScoreEnabled,
            onCheckedChange = { onStateChange(state.copy(consistencyScoreEnabled = it)) },
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
    onStateChange: (SettingsUiState) -> Unit,
) {
    SettingsSection(title = "DATA & PRIVACY") {
        Column(modifier = Modifier.fillMaxWidth()) {
            SleepToggle(
                label = "Firebase Sync",
                checked = state.firebaseSyncEnabled,
                onCheckedChange = { onStateChange(state.copy(firebaseSyncEnabled = it)) },
            )
            Text(
                text = "Last synced: Today",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
                modifier = Modifier.padding(start = Spacing.XS, bottom = Spacing.XS),
            )
        }
        SettingsDivider()
        SettingsLinkRow(
            label = "Export Data",
            onClick = { /* TODO: wire to ViewModel */ },
        )
    }
}

@Composable
private fun AboutSection() {
    SettingsSection(title = "ABOUT") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Version",
                style = SleepTypography.BodyLarge,
                color = SleepColors.White,
            )
            Text(
                text = APP_VERSION,
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        }
        SettingsDivider()
        SettingsLinkRow(
            label = "Privacy Policy",
            onClick = { /* TODO: wire to ViewModel */ },
        )
        SettingsDivider()
        SettingsLinkRow(
            label = "GitHub",
            onClick = { /* TODO: wire to ViewModel */ },
        )
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
        SettingsScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120, widthDp = 640, heightDp = 360)
@Composable
internal fun SettingsScreenLandscapePreview() {
    NightSkyTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
