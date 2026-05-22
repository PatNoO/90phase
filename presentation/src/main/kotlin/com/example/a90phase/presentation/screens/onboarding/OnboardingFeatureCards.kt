@file:Suppress("ForbiddenComment")

package com.example.a90phase.presentation.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.a90phase.presentation.components.PrimaryButton
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.rememberIsCompactHeight

private const val SMART_WAKE_BODY =
    "Your phone detects movement to wake you at a lighter moment within a window you set. " +
        "No microphone. No uploads. Completely local."
private const val SMART_WAKE_WARNING =
    "⚠ Phone must be on your bed to work. If you charge it away from bed — skip this, " +
        "it won't help you."
private const val DISCOVERY_BODY =
    "After 21 nights, 90phase learns your real sleep latency and cycle length — so bedtime " +
        "recommendations get sharper over time."

@Composable
internal fun OnboardingDailyCheckInCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
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
        OnboardingIconGlow(
            icon = "📅",
            iconSize = ICON_SIZE_SP.sp,
            glowRadius = FEATURE_GLOW_RADIUS,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = "Daily Check-in",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = "Every evening at 18:00 we ask: when do you need to wake up tomorrow?",
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = "Change the time or turn off in Settings.",
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = "Daily Check-in",
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
internal fun OnboardingBedtimeReminderCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
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
        OnboardingIconGlow(
            icon = "🌙",
            iconSize = ICON_SIZE_SP.sp,
            glowRadius = FEATURE_GLOW_RADIUS,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = "Bedtime Reminder",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = "We'll nudge you 15 minutes before your calculated bedtime so you can wind down.",
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = "Only fires on nights you've set a bedtime.",
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = "Bedtime Reminder",
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
internal fun OnboardingMorningCheckInCard(
    morningRatingEnabled: Boolean,
    morningBedtimeLogEnabled: Boolean,
    onMorningRatingToggle: (Boolean) -> Unit,
    onMorningBedtimeLogToggle: (Boolean) -> Unit,
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
        OnboardingIconGlow(
            icon = "☀️",
            iconSize = ICON_SIZE_SP.sp,
            glowRadius = FEATURE_GLOW_RADIUS,
            glowColor = SleepColors.GoodAmber,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = "Morning Check-in",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = "A quick check-in after you wake helps build your personal sleep profile.",
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = "How did you sleep? (1–5)",
            checked = morningRatingEnabled,
            onCheckedChange = onMorningRatingToggle,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        SleepToggle(
            label = "What time did you go to bed?",
            checked = morningBedtimeLogEnabled,
            onCheckedChange = onMorningBedtimeLogToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
internal fun OnboardingSmartWakeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
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
        OnboardingIconGlow(
            icon = "📳",
            iconSize = ICON_SIZE_SP.sp,
            glowRadius = FEATURE_GLOW_RADIUS,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = "Smart Wake",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = SMART_WAKE_BODY,
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = SMART_WAKE_WARNING,
            style = SleepTypography.BodyLarge,
            color = SleepColors.GoodAmber,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = "Smart Wake Window",
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
internal fun OnboardingDiscoveryCard(onGotIt: () -> Unit) {
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
        OnboardingIconGlow(
            icon = "✨",
            iconSize = ICON_SIZE_SP.sp,
            glowRadius = FEATURE_GLOW_RADIUS,
            glowColor = SleepColors.IndigoGlow,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        Text(
            text = "Discovery Phase",
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = DISCOVERY_BODY,
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = "Got it", onClick = onGotIt)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingDailyCheckInCardPreview() {
    NightSkyTheme {
        OnboardingDailyCheckInCard(enabled = true, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingDailyCheckInCardLandscapePreview() {
    NightSkyTheme {
        OnboardingDailyCheckInCard(enabled = false, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingBedtimeReminderCardPreview() {
    NightSkyTheme {
        OnboardingBedtimeReminderCard(enabled = false, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingBedtimeReminderCardLandscapePreview() {
    NightSkyTheme {
        OnboardingBedtimeReminderCard(enabled = true, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingMorningCheckInCardPreview() {
    NightSkyTheme {
        OnboardingMorningCheckInCard(
            morningRatingEnabled = true,
            morningBedtimeLogEnabled = false,
            onMorningRatingToggle = {},
            onMorningBedtimeLogToggle = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingMorningCheckInCardLandscapePreview() {
    NightSkyTheme {
        OnboardingMorningCheckInCard(
            morningRatingEnabled = false,
            morningBedtimeLogEnabled = true,
            onMorningRatingToggle = {},
            onMorningBedtimeLogToggle = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingSmartWakeCardPreview() {
    NightSkyTheme {
        OnboardingSmartWakeCard(enabled = false, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingSmartWakeCardLandscapePreview() {
    NightSkyTheme {
        OnboardingSmartWakeCard(enabled = true, onToggle = {}, onContinue = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040)
@Composable
internal fun OnboardingDiscoveryCardPreview() {
    NightSkyTheme {
        OnboardingDiscoveryCard(onGotIt = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1040, widthDp = 640, heightDp = 360)
@Composable
internal fun OnboardingDiscoveryCardLandscapePreview() {
    NightSkyTheme {
        OnboardingDiscoveryCard(onGotIt = {})
    }
}
