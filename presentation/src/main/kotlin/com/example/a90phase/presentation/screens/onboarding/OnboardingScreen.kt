@file:Suppress("ForbiddenComment")

package com.example.a90phase.presentation.screens.onboarding

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
import com.example.a90phase.presentation.components.PrimaryButton
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.OnboardingBackgroundGradient
import com.example.a90phase.presentation.theme.OnboardingNebulaWash
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.rememberIsCompactHeight
import kotlinx.coroutines.launch

private const val ONBOARDING_PAGE_COUNT = 8
private const val GLOW_RADIUS = 200f
internal const val FEATURE_GLOW_RADIUS = 160f
internal const val ICON_SIZE_SP = 72
private const val WELCOME_TAGLINE =
    "Wake up at the right moment in your sleep cycle — feeling rested, not groggy."
private const val WELCOME_NOTE =
    "Built around 90-minute cycles. Tuned to you over time."
private const val PERMISSIONS_SUBTITLE =
    "Grant access to enable check-ins, alarms, and reminders."

internal data class OnboardingUiState(
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val showWakeTimePicker: Boolean = false,
    val dailyCheckInEnabled: Boolean = false,
    val bedtimeReminderEnabled: Boolean = false,
    val morningRatingEnabled: Boolean = false,
    val morningBedtimeLogEnabled: Boolean = false,
    val smartWakeEnabled: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    // TODO: wire to ViewModel
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(OnboardingUiState()) }
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
                    onUiStateChange = { uiState = it },
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
                PageIndicator(
                    pageCount = ONBOARDING_PAGE_COUNT,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
    if (uiState.showWakeTimePicker) {
        WakeTimePickerDialog(
            initialHour = uiState.wakeHour,
            initialMinute = uiState.wakeMinute,
            onConfirm = { h, m ->
                uiState = uiState.copy(wakeHour = h, wakeMinute = m, showWakeTimePicker = false)
            },
            onDismiss = { uiState = uiState.copy(showWakeTimePicker = false) },
        )
    }
}

@Composable
private fun OnboardingTopBar(showBack: Boolean, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Small, vertical = Spacing.XS),
    ) {
        if (showBack) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Go back" },
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    uiState: OnboardingUiState,
    onUiStateChange: (OnboardingUiState) -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    when (page) {
        0 -> OnboardingWelcomePage(onGetStarted = onNext)
        1 -> OnboardingPermissionsPage(onContinue = onNext)
        2 -> OnboardingWakeTimePage(
            wakeHour = uiState.wakeHour,
            wakeMinute = uiState.wakeMinute,
            onTapTime = { onUiStateChange(uiState.copy(showWakeTimePicker = true)) },
            onContinue = onNext,
        )
        3 -> OnboardingDailyCheckInCard(
            enabled = uiState.dailyCheckInEnabled,
            onToggle = { onUiStateChange(uiState.copy(dailyCheckInEnabled = it)) }, // TODO: wire to ViewModel
            onContinue = onNext,
        )
        4 -> OnboardingBedtimeReminderCard(
            enabled = uiState.bedtimeReminderEnabled,
            onToggle = { onUiStateChange(uiState.copy(bedtimeReminderEnabled = it)) }, // TODO: wire to ViewModel
            onContinue = onNext,
        )
        5 -> OnboardingMorningCheckInCard(
            morningRatingEnabled = uiState.morningRatingEnabled,
            morningBedtimeLogEnabled = uiState.morningBedtimeLogEnabled,
            onMorningRatingToggle = { onUiStateChange(uiState.copy(morningRatingEnabled = it)) }, // TODO: wire to ViewModel
            onMorningBedtimeLogToggle = { onUiStateChange(uiState.copy(morningBedtimeLogEnabled = it)) }, // TODO: wire to ViewModel
            onContinue = onNext,
        )
        6 -> OnboardingSmartWakeCard(
            enabled = uiState.smartWakeEnabled,
            onToggle = { onUiStateChange(uiState.copy(smartWakeEnabled = it)) }, // TODO: wire to ViewModel
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
            text = "Sleep Cycle Optimizer",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Medium))
        Text(
            text = WELCOME_TAGLINE,
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = WELCOME_NOTE,
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Get started", onClick = onGetStarted)
    }
}

@Composable
private fun OnboardingPermissionsPage(onContinue: () -> Unit) {
    // TODO: wire to ViewModel — permission request launcher
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
            text = "Permissions",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = PERMISSIONS_SUBTITLE,
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        PermissionRow("🔔", "Notifications", "Required for check-ins and reminders", false)
        Spacer(modifier = Modifier.height(Spacing.Small))
        PermissionRow("⏰", "Exact Alarms", "Required for precise alarm timing", false)
        Spacer(modifier = Modifier.height(Spacing.Small))
        PermissionRow("📅", "Read Alarm", "Detects your existing alarm", true)
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
private fun OnboardingWakeTimePage(
    wakeHour: Int,
    wakeMinute: Int,
    onTapTime: () -> Unit,
    onContinue: () -> Unit,
) {
    // TODO: wire to ViewModel — pre-fill from system alarm
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
            text = "Wake Time",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = "When do you want to wake up?",
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        WakeTimeDisplay(hour = wakeHour, minute = wakeMinute, onClick = onTapTime)
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
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
                Text(
                    text = title,
                    style = SleepTypography.BodyLarge,
                    color = SleepColors.White,
                )
                if (isOptional) {
                    Text(
                        text = "  (optional)",
                        style = SleepTypography.BodyMedium,
                        color = SleepColors.Silver,
                    )
                }
            }
            Text(
                text = subtitle,
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        }
    }
}

@Composable
private fun WakeTimeDisplay(hour: Int, minute: Int, onClick: () -> Unit) {
    val timeText = "%02d:%02d".format(hour, minute)
    Text(
        text = timeText,
        style = SleepTypography.DisplayLarge,
        color = SleepColors.CyanGlow,
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = "$timeText, wake time, tap to change"
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
                text = "Wake Time",
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

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Page ${currentPage + 1} of $pageCount"
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

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingScreenPreview() {
    NightSkyTheme {
        OnboardingScreen(onComplete = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingScreenLandscapePreview() {
    NightSkyTheme {
        OnboardingScreen(onComplete = {})
    }
}
