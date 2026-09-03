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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.components.PrimaryButton
import com.example.a90phase.presentation.components.SleepToggle
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.rememberIsCompactHeight

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
            text = stringResource(R.string.onboarding_daily_checkin_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_daily_checkin_body),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = stringResource(R.string.onboarding_daily_checkin_note),
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = stringResource(R.string.onboarding_daily_checkin_title),
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.common_continue), onClick = onContinue)
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
            text = stringResource(R.string.onboarding_bedtime_reminder_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_bedtime_reminder_body),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = stringResource(R.string.onboarding_bedtime_reminder_note),
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = stringResource(R.string.onboarding_bedtime_reminder_title),
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.common_continue), onClick = onContinue)
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
            text = stringResource(R.string.onboarding_morning_checkin_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_morning_checkin_body),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = stringResource(R.string.onboarding_morning_rating_toggle),
            checked = morningRatingEnabled,
            onCheckedChange = onMorningRatingToggle,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        SleepToggle(
            label = stringResource(R.string.onboarding_morning_bedtime_toggle),
            checked = morningBedtimeLogEnabled,
            onCheckedChange = onMorningBedtimeLogToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.common_continue), onClick = onContinue)
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
            text = stringResource(R.string.onboarding_smart_wake_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_smart_wake_body),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_smart_wake_warning),
            style = SleepTypography.BodyLarge,
            color = SleepColors.GoodAmber,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        SleepToggle(
            label = stringResource(R.string.settings_smart_wake),
            checked = enabled,
            onCheckedChange = onToggle,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.common_continue), onClick = onContinue)
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
            text = stringResource(R.string.onboarding_discovery_title),
            style = SleepTypography.HeadlineLarge,
            color = SleepColors.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = stringResource(R.string.onboarding_discovery_body),
            style = SleepTypography.BodyLarge,
            color = SleepColors.Silver,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.Medium else Spacing.XL))
        PrimaryButton(text = stringResource(R.string.onboarding_got_it), onClick = onGotIt)
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
