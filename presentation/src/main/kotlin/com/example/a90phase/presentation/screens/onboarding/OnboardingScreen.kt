package com.example.a90phase.presentation.screens.onboarding

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.components.PrimaryButton
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.OnboardingBackgroundGradient
import com.example.a90phase.presentation.theme.OnboardingNebulaWash
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.rememberIsCompactHeight
import com.example.a90phase.presentation.viewmodels.OnboardingFeature
import com.example.a90phase.presentation.viewmodels.OnboardingUiState
import com.example.a90phase.presentation.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch

private const val ONBOARDING_PAGE_COUNT = 8
private const val PERMISSIONS_PAGE = 1
private const val GLOW_RADIUS = 200f
internal const val FEATURE_GLOW_RADIUS = 160f
internal const val ICON_SIZE_SP = 72

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    var showNotifRationale by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun goNext() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
    fun goBack() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
    LaunchedEffect(uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) onComplete()
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == PERMISSIONS_PAGE) {
            checkAndRequestNotificationPermission(context, notifLauncher) { showNotifRationale = true }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = OnboardingBackgroundGradient),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = OnboardingNebulaWash))
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingTopBar(showBack = pagerState.currentPage > 0, onBack = ::goBack)
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(
                    page = page,
                    uiState = uiState,
                    onFeatureEnabled = viewModel::onFeatureEnabled,
                    onShowWakeTimePicker = viewModel::onShowWakeTimePicker,
                    onNext = ::goNext,
                    onComplete = viewModel::onOnboardingComplete,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.Large),
                horizontalArrangement = Arrangement.Center,
            ) {
                PageIndicator(pageCount = ONBOARDING_PAGE_COUNT, currentPage = pagerState.currentPage)
            }
        }
    }
    OnboardingDialogs(
        uiState = uiState,
        showNotifRationale = showNotifRationale,
        onWakeTimeConfirmed = { h, m -> viewModel.onWakeTimeSelected(h, m); viewModel.onShowWakeTimePicker(false) },
        onWakeTimeDismissed = { viewModel.onShowWakeTimePicker(false) },
        onNotifRationaleConfirmed = { showNotifRationale = false; notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onNotifRationaleDismissed = { showNotifRationale = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingDialogs(
    uiState: OnboardingUiState,
    showNotifRationale: Boolean,
    onWakeTimeConfirmed: (Int, Int) -> Unit,
    onWakeTimeDismissed: () -> Unit,
    onNotifRationaleConfirmed: () -> Unit,
    onNotifRationaleDismissed: () -> Unit,
) {
    if (uiState.showWakeTimePicker) {
        WakeTimePickerDialog(
            initialHour = uiState.wakeHour,
            initialMinute = uiState.wakeMinute,
            onConfirm = onWakeTimeConfirmed,
            onDismiss = onWakeTimeDismissed,
        )
    }
    if (showNotifRationale) {
        PermissionRationaleDialog(
            onConfirm = onNotifRationaleConfirmed,
            onDismiss = onNotifRationaleDismissed,
        )
    }
}

private fun checkAndRequestNotificationPermission(
    context: android.content.Context,
    launcher: ActivityResultLauncher<String>,
    onShowRationale: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    val shouldShowRationale = (context as? Activity)?.shouldShowRequestPermissionRationale(
        Manifest.permission.POST_NOTIFICATIONS,
    ) == true
    when {
        granted -> Unit
        shouldShowRationale -> onShowRationale()
        else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun PermissionRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.onboarding_notifications_needed_title),
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.onboarding_notifications_needed_body),
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.onboarding_grant), color = SleepColors.CyanGlow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_skip), color = SleepColors.Silver)
            }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Composable
private fun OnboardingTopBar(showBack: Boolean, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Small, vertical = Spacing.XS),
    ) {
        if (showBack) {
            val goBackDescription = stringResource(R.string.onboarding_go_back)
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = goBackDescription },
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    uiState: OnboardingUiState,
    onFeatureEnabled: (OnboardingFeature, Boolean) -> Unit,
    onShowWakeTimePicker: (Boolean) -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    when (page) {
        0 -> OnboardingWelcomePage(onGetStarted = onNext)
        1 -> OnboardingPermissionsPage(onContinue = onNext)
        2 -> OnboardingWakeTimePage(
            wakeHour = uiState.wakeHour,
            wakeMinute = uiState.wakeMinute,
            onTapTime = { onShowWakeTimePicker(true) },
            onContinue = onNext,
        )
        3 -> OnboardingDailyCheckInCard(
            enabled = uiState.dailyCheckInEnabled,
            onToggle = { onFeatureEnabled(OnboardingFeature.DailyCheckIn, it) },
            onContinue = onNext,
        )
        4 -> OnboardingBedtimeReminderCard(
            enabled = uiState.bedtimeReminderEnabled,
            onToggle = { onFeatureEnabled(OnboardingFeature.BedtimeReminder, it) },
            onContinue = onNext,
        )
        5 -> OnboardingMorningCheckInCard(
            morningRatingEnabled = uiState.morningRatingEnabled,
            morningBedtimeLogEnabled = uiState.morningBedtimeLogEnabled,
            onMorningRatingToggle = { onFeatureEnabled(OnboardingFeature.MorningRating, it) },
            onMorningBedtimeLogToggle = { onFeatureEnabled(OnboardingFeature.MorningBedtimeLog, it) },
            onContinue = onNext,
        )
        6 -> OnboardingSmartWakeCard(
            enabled = uiState.smartWakeEnabled,
            onToggle = { onFeatureEnabled(OnboardingFeature.SmartWake, it) },
            onContinue = onNext,
        )
        else -> OnboardingDiscoveryCard(onGotIt = onComplete)
    }
}

@Composable
private fun OnboardingWelcomePage(onGetStarted: () -> Unit) {
    val isCompact = rememberIsCompactHeight()
    val sectionSpacing = if (isCompact) Spacing.Medium else Spacing.Large
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Large)
            .padding(vertical = sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingIconGlow(icon = "🌙", iconSize = ICON_SIZE_SP.sp, glowRadius = GLOW_RADIUS)
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        Text(
            text = stringResource(R.string.onboarding_welcome_tagline),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = stringResource(R.string.onboarding_welcome_note),
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.onboarding_get_started), onClick = onGetStarted)
    }
}

@Composable
private fun OnboardingPermissionsPage(onContinue: () -> Unit) {
    val context = LocalContext.current
    val isCompact = rememberIsCompactHeight()
    val sectionSpacing = if (isCompact) Spacing.Medium else Spacing.Large
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Large)
            .padding(vertical = sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingIconGlow(icon = "🛡", iconSize = ICON_SIZE_SP.sp, glowRadius = GLOW_RADIUS)
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = stringResource(R.string.onboarding_permissions_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_permissions_subtitle),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        PermissionRows()
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(
            text = stringResource(R.string.common_continue),
            onClick = { requestExactAlarmsIfNeeded(context); onContinue() },
        )
    }
}

@Composable
private fun PermissionRows() {
    PermissionRow(
        "🔔",
        stringResource(R.string.onboarding_permission_notifications_title),
        stringResource(R.string.onboarding_permission_notifications_subtitle),
        false,
    )
    Spacer(modifier = Modifier.height(Spacing.Small))
    PermissionRow(
        "⏰",
        stringResource(R.string.onboarding_permission_alarms_title),
        stringResource(R.string.onboarding_permission_alarms_subtitle),
        false,
    )
    Spacer(modifier = Modifier.height(Spacing.Small))
    PermissionRow(
        "📅",
        stringResource(R.string.onboarding_permission_read_alarm_title),
        stringResource(R.string.onboarding_permission_read_alarm_subtitle),
        true,
    )
}

private fun requestExactAlarmsIfNeeded(context: android.content.Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (!alarmManager.canScheduleExactAlarms()) {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }
}

@Composable
private fun OnboardingWakeTimePage(
    wakeHour: Int,
    wakeMinute: Int,
    onTapTime: () -> Unit,
    onContinue: () -> Unit,
) {
    val isCompact = rememberIsCompactHeight()
    val sectionSpacing = if (isCompact) Spacing.Medium else Spacing.Large
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.Large)
            .padding(vertical = sectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OnboardingIconGlow(icon = "⏰", iconSize = ICON_SIZE_SP.sp, glowRadius = GLOW_RADIUS)
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = stringResource(R.string.onboarding_wake_time_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_wake_time_subtitle),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        WakeTimeDisplay(hour = wakeHour, minute = wakeMinute, onClick = onTapTime)
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.common_continue), onClick = onContinue)
    }
}

@Composable
internal fun OnboardingIconGlow(
    icon: String,
    iconSize: TextUnit,
    glowRadius: Float,
    glowColor: Color = SleepColors.CyanGlow,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clearAndSetSemantics { }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        radius = glowRadius,
                    ),
                )
            },
    ) {
        Text(text = icon, fontSize = iconSize, color = glowColor)
    }
}

@Composable
private fun PermissionRow(icon: String, title: String, subtitle: String, isOptional: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, style = SleepTypography.HeadlineMedium)
        Spacer(modifier = Modifier.width(Spacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = SleepTypography.BodyLarge, color = SleepColors.White)
                if (isOptional) {
                    // Explicit Spacer — AAPT strips leading whitespace from string resources.
                    Spacer(modifier = Modifier.width(Spacing.XXS))
                    Text(
                        text = stringResource(R.string.onboarding_permission_optional),
                        style = SleepTypography.BodyMedium,
                        color = SleepColors.Silver,
                    )
                }
            }
            Text(text = subtitle, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
        }
    }
}

@Composable
private fun WakeTimeDisplay(hour: Int, minute: Int, onClick: () -> Unit) {
    val timeText = "%02d:%02d".format(hour, minute)
    val description = stringResource(R.string.onboarding_wake_time_description, timeText)
    Text(
        text = timeText,
        style = SleepTypography.DisplayLarge,
        color = SleepColors.CyanGlow,
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
            .clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WakeTimePickerDialog(
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
                text = stringResource(R.string.onboarding_wake_time_title),
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
                Text(text = stringResource(R.string.common_confirm), color = SleepColors.CyanGlow)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.common_cancel), color = SleepColors.Silver) }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.onboarding_page_indicator_description, currentPage + 1, pageCount)
    Row(
        modifier = modifier.semantics {
            contentDescription = description
        },
        horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) SleepColors.CyanGlow else SleepColors.White.copy(alpha = 0.3f),
                    ),
            )
        }
    }
}

@Composable
internal fun OnboardingScreenContent(uiState: OnboardingUiState, onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    fun goNext() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
    fun goBack() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = OnboardingBackgroundGradient),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = OnboardingNebulaWash))
        Column(modifier = Modifier.fillMaxSize()) {
            OnboardingTopBar(showBack = pagerState.currentPage > 0, onBack = ::goBack)
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                OnboardingPageContent(
                    page = page,
                    uiState = uiState,
                    onFeatureEnabled = { _, _ -> },
                    onShowWakeTimePicker = {},
                    onNext = ::goNext,
                    onComplete = onComplete,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.Large),
                horizontalArrangement = Arrangement.Center,
            ) {
                PageIndicator(pageCount = ONBOARDING_PAGE_COUNT, currentPage = pagerState.currentPage)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingScreenPreview() {
    NightSkyTheme {
        OnboardingScreenContent(uiState = OnboardingUiState(), onComplete = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingScreenLandscapePreview() {
    NightSkyTheme {
        OnboardingScreenContent(uiState = OnboardingUiState(), onComplete = {})
    }
}
