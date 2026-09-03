package com.example.a90phase.presentation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.util.CARD_DATE_PATTERN
import com.example.a90phase.presentation.util.formatSleepDuration
import com.example.a90phase.presentation.util.rememberDateFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val SystemZone = ZoneId.systemDefault()

private fun Instant.toLocalTime() =
    LocalDateTime.ofInstant(this, SystemZone).toLocalTime()

@Composable
fun SleepLogCard(
    log: SleepLog,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.XXS)
            .glassCard()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .semantics(mergeDescendants = true) { }
            .padding(Spacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = log.date.format(rememberDateFormatter(CARD_DATE_PATTERN)),
                style = SleepTypography.BodyLarge,
                color = SleepColors.White,
                modifier = Modifier.weight(1f),
            )
            StarRating(rating = log.qualityRating)
        }
        Spacer(modifier = Modifier.height(Spacing.XS))
        val bedtimeText = log.bedtime?.toLocalTime()?.format(TimeFormatter)
            ?: stringResource(R.string.sleep_log_no_bedtime)
        val wakeTimeText = log.wakeTime.toLocalTime().format(TimeFormatter)
        Text(
            text = stringResource(R.string.sleep_log_slept_woke, bedtimeText, wakeTimeText),
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
        )
        val durationText = formatSleepDuration(log.sleepDurationMinutes)
        val cyclesText = pluralStringResource(R.plurals.cycles_count, log.cycleCount, log.cycleCount)
        Text(
            text = stringResource(R.string.sleep_log_summary, cyclesText, durationText),
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
