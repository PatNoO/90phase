package com.example.a90phase.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CARD_RADIUS = 24.dp
private val CARD_SHAPE = RoundedCornerShape(CARD_RADIUS)
private const val GLOW_RADIUS = 400f

@Composable
fun WakeTimeCard(
    time: LocalTime,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "GlowAlpha",
    )
    val glowAlpha = if (isActive) pulseAlpha else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SleepColors.CyanGlow.copy(alpha = glowAlpha),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = GLOW_RADIUS,
                    ),
                )
            },
    ) {
        WakeTimeCardInner(time = time, isActive = isActive, onClick = onClick)
    }
}

@Composable
private fun WakeTimeCardInner(time: LocalTime, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = CARD_RADIUS)
            .border(
                width = if (isActive) 1.dp else 0.5.dp,
                color = if (isActive) {
                    SleepColors.CyanGlow.copy(alpha = 0.6f)
                } else {
                    SleepColors.White.copy(alpha = 0.08f)
                },
                shape = CARD_SHAPE,
            )
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.XXL, horizontal = Spacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedContent(
                targetState = time,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn(tween(200)) togetherWith
                        slideOutVertically { height -> -height } + fadeOut(tween(200))
                },
                label = "WakeTimeTransition",
            ) { targetTime ->
                Text(
                    text = targetTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.DisplayLarge,
                    color = if (isActive) SleepColors.CyanGlow else SleepColors.White,
                )
            }
            Text(
                text = "VAKNA-TID  ·  TRYCK FÖR ATT ÄNDRA",
                style = SleepTypography.LabelMedium,
                color = SleepColors.Silver.copy(alpha = 0.5f),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun WakeTimeCardActivePreview() {
    NightSkyTheme {
        WakeTimeCard(
            time = LocalTime.of(7, 0),
            isActive = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun WakeTimeCardInactivePreview() {
    NightSkyTheme {
        WakeTimeCard(
            time = LocalTime.of(7, 0),
            isActive = false,
            onClick = {},
        )
    }
}
