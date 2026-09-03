package com.example.a90phase.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.presentation.components.StarRating
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.util.formatSleepDuration
import com.example.a90phase.presentation.viewmodels.LogDetailEvent
import com.example.a90phase.presentation.viewmodels.LogDetailUiState
import com.example.a90phase.presentation.viewmodels.LogDetailViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DetailDateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH)
private val DetailTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DetailZone = ZoneId.systemDefault()

private fun Instant.toDetailTime() = LocalDateTime.ofInstant(this, DetailZone).toLocalTime()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogDetailEvent.Deleted -> onNavigateBack()
                is LogDetailEvent.DeleteFailed -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LogDetailTopBar(
                    onNavigateBack = onNavigateBack,
                    onDeleteClick = { showDeleteConfirmation = true },
                    showDeleteAction = uiState is LogDetailUiState.Content,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (val state = uiState) {
                is LogDetailUiState.Loading -> LogDetailLoadingState(Modifier.padding(innerPadding))
                is LogDetailUiState.Content -> LogDetailContent(
                    log = state.log,
                    modifier = Modifier.padding(innerPadding),
                )
                is LogDetailUiState.NotFound -> LogDetailMessageState(
                    message = "Sleep log not found",
                    modifier = Modifier.padding(innerPadding),
                )
                is LogDetailUiState.Error -> LogDetailMessageState(
                    message = state.message,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteLogConfirmationDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.onDeleteConfirmed()
            },
            onDismiss = { showDeleteConfirmation = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogDetailTopBar(
    onNavigateBack: () -> Unit,
    onDeleteClick: () -> Unit,
    showDeleteAction: Boolean,
) {
    TopAppBar(
        title = {
            Text(
                text = "Sleep Log",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Navigate back" },
                )
            }
        },
        actions = {
            if (showDeleteAction) {
                IconButton(onClick = onDeleteClick) {
                    Text(
                        text = "🗑",
                        style = SleepTypography.HeadlineMedium,
                        color = SleepColors.ErrorRed,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = "Delete sleep log" },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun LogDetailContent(log: SleepLog, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(Spacing.Medium)) {
        Box(modifier = Modifier.fillMaxWidth().glassCard().padding(Spacing.Medium)) {
            Column {
                Text(
                    text = log.date.format(DetailDateFormatter),
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.White,
                )
                Spacer(Modifier.height(Spacing.Small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Rating", style = SleepTypography.BodyMedium, color = SleepColors.Silver)
                    if (log.hasBeenRated()) {
                        StarRating(rating = log.qualityRating)
                    } else {
                        Text(text = "Unrated", style = SleepTypography.BodyMedium, color = SleepColors.SlateBlue)
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.Medium))
        Box(modifier = Modifier.fillMaxWidth().glassCard().padding(Spacing.Medium)) {
            Column {
                DetailRow(
                    label = "Bedtime",
                    value = log.bedtime?.toDetailTime()?.format(DetailTimeFormatter) ?: "—",
                )
                Spacer(Modifier.height(Spacing.Small))
                DetailRow(label = "Wake time", value = log.wakeTime.toDetailTime().format(DetailTimeFormatter))
                Spacer(Modifier.height(Spacing.Small))
                DetailRow(label = "Cycles", value = "${log.cycleCount}")
                Spacer(Modifier.height(Spacing.Small))
                DetailRow(label = "Total duration", value = formatSleepDuration(log.sleepDurationMinutes))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
        Text(text = value, style = SleepTypography.BodyMedium, color = SleepColors.White)
    }
}

@Composable
private fun DeleteLogConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Sleep Log", style = SleepTypography.HeadlineMedium, color = SleepColors.White)
        },
        text = {
            Text(
                text = "This cannot be undone. Are you sure you want to delete this log?",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = SleepColors.ErrorRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = SleepColors.Silver)
            }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Composable
private fun LogDetailLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SleepColors.CyanGlow)
    }
}

@Composable
private fun LogDetailMessageState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = SleepTypography.BodyLarge, color = SleepColors.ErrorRed)
    }
}

private val PreviewLog = SleepLog(
    id = "1",
    date = LocalDate.of(2025, 5, 8),
    bedtime = Instant.parse("2025-05-07T21:15:00Z"),
    wakeTime = Instant.parse("2025-05-08T05:25:00Z"),
    qualityRating = 4,
    cycleCount = 6,
    cycleDurationUsed = 90,
    sleepLatencyUsed = 15,
    syncStatus = SyncStatus.SYNCED,
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun LogDetailContentPreview() {
    NightSkyTheme {
        LogDetailContent(log = PreviewLog)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun LogDetailUnratedPreview() {
    NightSkyTheme {
        LogDetailContent(log = PreviewLog.copy(qualityRating = null, bedtime = null))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun LogDetailLoadingPreview() {
    NightSkyTheme {
        LogDetailLoadingState()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun LogDetailNotFoundPreview() {
    NightSkyTheme {
        LogDetailMessageState(message = "Sleep log not found")
    }
}
