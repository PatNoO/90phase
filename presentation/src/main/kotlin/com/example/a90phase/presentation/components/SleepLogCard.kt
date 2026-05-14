package com.example.a90phase.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SwedishLocale = Locale("sv")
private val DateFormatter = DateTimeFormatter.ofPattern("EEEE  d MMM", SwedishLocale)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val SystemZone = ZoneId.systemDefault()

private fun Instant.toLocalTime() =
    LocalDateTime.ofInstant(this, SystemZone).toLocalTime()

@Composable
fun SleepLogCard(
    log: SleepLog,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard()
            .padding(Spacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = log.date.format(DateFormatter),
                style = SleepTypography.BodyLarge,
                color = SleepColors.White,
                modifier = Modifier.weight(1f),
            )
            StarRating(rating = log.qualityRating)
        }
        Spacer(modifier = Modifier.height(Spacing.XS))
        val bedtimeText = log.bedtime?.toLocalTime()?.format(TimeFormatter) ?: "--:--"
        val wakeTimeText = log.wakeTime.toLocalTime().format(TimeFormatter)
        Text(
            text = "Sov $bedtimeText  →  Vaknade $wakeTimeText",
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
        )
        val totalMinutes = log.cycleCount * log.cycleDurationUsed
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val durationText = if (minutes == 0) "${hours}h" else "${hours}h ${minutes}min"
        Text(
            text = "${log.cycleCount} cykler  ·  $durationText",
            style = SleepTypography.BodyMedium,
            color = SleepColors.SlateBlue,
        )
    }
}

private val PreviewLog = SleepLog(
    id = "1",
    date = LocalDate.of(2025, 5, 8),
    bedtime = Instant.parse("2025-05-07T21:15:00Z"),
    wakeTime = Instant.parse("2025-05-08T05:25:00Z"),
    qualityRating = 5,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SleepLogCardPreview() {
    NightSkyTheme {
        SleepLogCard(log = PreviewLog)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun SleepLogCardNoRatingPreview() {
    NightSkyTheme {
        SleepLogCard(log = PreviewLog.copy(qualityRating = null, bedtime = null))
    }
}
