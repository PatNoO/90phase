package com.example.a90phase.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.OptimalCardGradient
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepShapes
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun BedtimeResultCard(
    time: LocalTime,
    cycleCount: Int,
    durationLabel: String,
    quality: BedtimeQuality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPassed = quality == BedtimeQuality.PASSED

    val cardBackground = when (quality) {
        BedtimeQuality.OPTIMAL -> OptimalCardGradient
        else -> Brush.horizontalGradient(
            listOf(SleepColors.MidnightBlue, SleepColors.MidnightBlue),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard(cornerRadius = 16.dp)
            .background(cardBackground, SleepShapes.Large)
            .clickable(enabled = !isPassed, onClick = onClick)
            .padding(Spacing.Medium),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = SleepTypography.DisplayMedium,
                    color = if (isPassed) SleepColors.SlateBlue else SleepColors.White,
                    textDecoration = if (isPassed) TextDecoration.LineThrough else null,
                )
                Text(
                    text = "$cycleCount cykler",
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
            }
            Spacer(modifier = Modifier.height(Spacing.XS))
            HorizontalDivider(color = SleepColors.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(Spacing.XS))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QualityBadge(quality = quality)
                Text(
                    text = durationLabel,
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
            }
        }
    }
}

@Composable
fun QualityBadge(
    quality: BedtimeQuality,
    modifier: Modifier = Modifier,
) {
    val (color, label) = when (quality) {
        BedtimeQuality.OPTIMAL -> SleepColors.OptimalGreen to "OPTIMAL"
        BedtimeQuality.GOOD -> SleepColors.GoodAmber to "BRA"
        BedtimeQuality.MINIMAL -> SleepColors.MinimalSlate to "MINIMUM"
        BedtimeQuality.PASSED -> SleepColors.PassedGray to "PASSERAD"
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), SleepShapes.Pill)
            .border(0.5.dp, color.copy(alpha = 0.4f), SleepShapes.Pill)
            .padding(horizontal = Spacing.Small, vertical = Spacing.XXS),
    ) {
        Text(
            text = label,
            style = SleepTypography.LabelMedium,
            color = color,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun BedtimeOptimalPreview() {
    NightSkyTheme {
        BedtimeResultCard(
            time = LocalTime.of(23, 15),
            cycleCount = 6,
            durationLabel = "7h 30min sömn",
            quality = BedtimeQuality.OPTIMAL,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun BedtimeGoodPreview() {
    NightSkyTheme {
        BedtimeResultCard(
            time = LocalTime.of(0, 45),
            cycleCount = 5,
            durationLabel = "6h 00min sömn",
            quality = BedtimeQuality.GOOD,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun BedtimePassedPreview() {
    NightSkyTheme {
        BedtimeResultCard(
            time = LocalTime.of(21, 45),
            cycleCount = 7,
            durationLabel = "Passerad",
            quality = BedtimeQuality.PASSED,
            onClick = {},
        )
    }
}
