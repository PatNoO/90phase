package com.example.a90phase.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepShapes
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.TimeGlowGradient
import com.example.a90phase.presentation.theme.glassCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CardRadius = 24.dp
private val CardShape = RoundedCornerShape(CardRadius)
private val GlowSize = 240.dp

@Composable
fun WakeTimeCard(
    time: LocalTime,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(GlowSize)
                    .background(TimeGlowGradient, SleepShapes.Circle),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(cornerRadius = CardRadius)
                .border(
                    width = if (isActive) 1.dp else 0.5.dp,
                    color = if (isActive) {
                        SleepColors.CyanGlow.copy(alpha = 0.6f)
                    } else {
                        SleepColors.White.copy(alpha = 0.08f)
                    },
                    shape = CardShape,
                )
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.XXL, horizontal = Spacing.Large),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.DisplayLarge,
                    color = if (isActive) SleepColors.CyanGlow else SleepColors.White,
                )
                Text(
                    text = "VAKNA-TID  ·  TRYCK FÖR ATT ÄNDRA",
                    style = SleepTypography.LabelMedium,
                    color = SleepColors.Silver.copy(alpha = 0.5f),
                )
            }
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
