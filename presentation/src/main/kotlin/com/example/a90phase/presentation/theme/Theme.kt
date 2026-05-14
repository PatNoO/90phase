package com.example.a90phase.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NightSkyColorScheme = darkColorScheme(
    primary = SleepColors.CyanGlow,
    onPrimary = SleepColors.DeepSpace,
    primaryContainer = SleepColors.CyanSoft,
    onPrimaryContainer = SleepColors.White,

    secondary = SleepColors.IndigoGlow,
    onSecondary = SleepColors.White,
    secondaryContainer = SleepColors.NebulaPurple,
    onSecondaryContainer = SleepColors.White,

    tertiary = SleepColors.NebulaViolet,
    onTertiary = SleepColors.White,

    background = SleepColors.NavyBlue,
    onBackground = SleepColors.White,

    surface = SleepColors.MidnightBlue,
    onSurface = SleepColors.White,
    surfaceVariant = SleepColors.GlassSurface,
    onSurfaceVariant = SleepColors.Silver,

    outline = SleepColors.SlateBlue,
    error = SleepColors.ErrorRed,
    onError = SleepColors.White,
)

@Composable
fun NightSkyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightSkyColorScheme,
        content = content,
    )
}
