package com.example.a90phase.presentation.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        SleepColors.DeepSpace,
        SleepColors.NavyBlue,
        Color(0xFF0F1A30),
    ),
)

val OnboardingBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        SleepColors.NebulaDeep,
        Color(0xFF120B30),
        SleepColors.NavyBlue,
    ),
)

val OnboardingNebulaWash = Brush.radialGradient(
    colors = listOf(
        SleepColors.NebulaViolet.copy(alpha = 0.25f),
        SleepColors.NebulaPurple.copy(alpha = 0.12f),
        Color.Transparent,
    ),
    center = Offset(x = 0.75f, y = 0.25f),
    radius = 600f,
)

val TimeGlowGradient = Brush.radialGradient(
    colors = listOf(
        SleepColors.CyanGlow.copy(alpha = 0.15f),
        Color.Transparent,
    ),
    radius = 320f,
)

val OptimalCardGradient = Brush.horizontalGradient(
    colors = listOf(
        SleepColors.OptimalGreen.copy(alpha = 0.12f),
        SleepColors.CyanGlow.copy(alpha = 0.05f),
    ),
)

val GlassCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x22FFFFFF),
        Color(0x08FFFFFF),
    ),
)
