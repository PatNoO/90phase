package com.example.a90phase.presentation.components

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.OptimalCardGradient
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepShapes
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val overshootEasing = Easing { fraction ->
    OvershootInterpolator(2f).getInterpolation(fraction)
}

@Composable
fun BedtimeResultCard(
    time: LocalTime,
    cycleCount: Int,
    durationLabel: String,
    quality: BedtimeQuality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val isPassed = quality == BedtimeQuality.PASSED
    val cardBackground = when (quality) {
        BedtimeQuality.OPTIMAL -> OptimalCardGradient
        else -> Brush.horizontalGradient(listOf(SleepColors.MidnightBlue, SleepColors.MidnightBlue))
    }
    val checkmarkProgress by animateFloatAsState(
        targetValue = if (isSelected && !isPassed) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = overshootEasing),
        label = "CheckmarkProgress",
    )

    val timeFormatted = time.format(DateTimeFormatter.ofPattern("HH:mm"))
    val qualityLabel = qualityLabel(quality)
    val cyclesLabel = pluralStringResource(R.plurals.cycles_count, cycleCount, cycleCount)
    val description = stringResource(R.string.bedtime_card_description, timeFormatted, cyclesLabel, qualityLabel, durationLabel)
    val passedState = stringResource(R.string.bedtime_card_state_passed)
    val selectedState = stringResource(R.string.bedtime_card_state_selected)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard(cornerRadius = 16.dp)
            .background(cardBackground, SleepShapes.Large)
            .semantics {
                contentDescription = description
                stateDescription = when {
                    isPassed -> passedState
                    isSelected -> selectedState
                    else -> ""
                }
                role = Role.Button
            }
            .clickable(enabled = !isPassed, onClick = onClick)
            .padding(Spacing.Medium),
    ) {
        Column {
            BedtimeCardTopRow(
                time = time,
                cycleCount = cycleCount,
                isPassed = isPassed,
                checkmarkProgress = checkmarkProgress,
            )
            Spacer(modifier = Modifier.height(Spacing.XS))
            HorizontalDivider(color = SleepColors.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(Spacing.XS))
            BedtimeCardBottomRow(quality = quality, durationLabel = durationLabel)
        }
    }
}

@Composable
private fun BedtimeCardTopRow(time: LocalTime, cycleCount: Int, isPassed: Boolean, checkmarkProgress: Float) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (checkmarkProgress > 0f) {
                CheckmarkCanvas(progress = checkmarkProgress)
            }
            Text(
                text = pluralStringResource(R.plurals.cycles_count, cycleCount, cycleCount),
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        }
    }
}

@Composable
private fun BedtimeCardBottomRow(quality: BedtimeQuality, durationLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QualityBadge(quality = quality)
        Text(text = durationLabel, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
    }
}

@Composable
private fun CheckmarkCanvas(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val p1 = Offset(size.width * 0.17f, size.height * 0.52f)
        val p2 = Offset(size.width * 0.42f, size.height * 0.75f)
        val p3 = Offset(size.width * 0.83f, size.height * 0.28f)
        val leg1Length = (p2 - p1).getDistance()
        val leg2Length = (p3 - p2).getDistance()
        val drawnLength = (leg1Length + leg2Length) * progress.coerceIn(0f, 1f)
        val path = Path()
        path.moveTo(p1.x, p1.y)
        if (drawnLength <= leg1Length) {
            val t = drawnLength / leg1Length
            path.lineTo(p1.x + (p2.x - p1.x) * t, p1.y + (p2.y - p1.y) * t)
        } else {
            path.lineTo(p2.x, p2.y)
            val t = ((drawnLength - leg1Length) / leg2Length).coerceIn(0f, 1f)
            path.lineTo(p2.x + (p3.x - p2.x) * t, p2.y + (p3.y - p2.y) * t)
        }
        drawPath(
            path = path,
            color = SleepColors.OptimalGreen,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun qualityLabel(quality: BedtimeQuality): String = stringResource(
    when (quality) {
        BedtimeQuality.OPTIMAL -> R.string.bedtime_quality_optimal
        BedtimeQuality.GOOD -> R.string.bedtime_quality_good
        BedtimeQuality.MINIMAL -> R.string.bedtime_quality_minimal
        BedtimeQuality.PASSED -> R.string.bedtime_quality_passed
    },
)

@Composable
fun QualityBadge(
    quality: BedtimeQuality,
    modifier: Modifier = Modifier,
) {
    val color = when (quality) {
        BedtimeQuality.OPTIMAL -> SleepColors.OptimalGreen
        BedtimeQuality.GOOD -> SleepColors.GoodAmber
        BedtimeQuality.MINIMAL -> SleepColors.MinimalSlate
        BedtimeQuality.PASSED -> SleepColors.PassedGray
    }
    val label = qualityLabel(quality)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), SleepShapes.Pill)
            .border(0.5.dp, color.copy(alpha = 0.4f), SleepShapes.Pill)
            .padding(horizontal = Spacing.Small, vertical = Spacing.XXS),
    ) {
        Text(text = label, style = SleepTypography.LabelMedium, color = color)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun BedtimeOptimalPreview() {
    NightSkyTheme {
        BedtimeResultCard(
            time = LocalTime.of(23, 15),
            cycleCount = 6,
            durationLabel = "7h 30min sleep",
            quality = BedtimeQuality.OPTIMAL,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun BedtimeOptimalSelectedPreview() {
    NightSkyTheme {
        BedtimeResultCard(
            time = LocalTime.of(23, 15),
            cycleCount = 6,
            durationLabel = "7h 30min sleep",
            quality = BedtimeQuality.OPTIMAL,
            isSelected = true,
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
            durationLabel = "6h 00min sleep",
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
            durationLabel = "Passed",
            quality = BedtimeQuality.PASSED,
            onClick = {},
        )
    }
}
