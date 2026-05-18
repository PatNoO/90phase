package com.example.a90phase.presentation.screens.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.presentation.theme.BackgroundGradient
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.viewmodels.DiscoveryResultsUiState
import com.example.a90phase.presentation.viewmodels.DiscoveryResultsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryResultsScreen(
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: DiscoveryResultsViewModel = hiltViewModel(),
) {
    val vmState by viewModel.uiState.collectAsStateWithLifecycle()
    val isApplied = (vmState as? DiscoveryResultsUiState.Ready)?.isApplied ?: false
    LaunchedEffect(isApplied) {
        if (isApplied) onApply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient),
    ) {
        StarFieldBackground()
        Scaffold(
            topBar = { DiscoveryResultsTopBar(onDismiss = onDismiss) },
            containerColor = Color.Transparent,
        ) { padding ->
            when (val state = vmState) {
                is DiscoveryResultsUiState.Loading -> Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SleepColors.CyanGlow)
                }
                is DiscoveryResultsUiState.Error -> Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = SleepColors.ErrorRed,
                        style = SleepTypography.BodyLarge,
                    )
                }
                is DiscoveryResultsUiState.Ready -> {
                    DiscoveryResultsContent(
                        state = state,
                        onApply = viewModel::onApply,
                        onDismiss = onDismiss,
                        modifier = Modifier.padding(padding),
                    )
                    if (state.applyError != null) {
                        ApplyErrorDialog(
                            message = state.applyError,
                            onDismiss = onDismiss,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoveryResultsTopBar(onDismiss: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Discovery Phase Complete",
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.White,
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Text(
                    text = "←",
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.Silver,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Dismiss"
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SleepColors.MidnightBlue,
        ),
    )
}

@Composable
private fun DiscoveryResultsContent(
    state: DiscoveryResultsUiState.Ready,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        item { WinnerSection(state = state) }
        item { RatingsSection(state = state) }
        item { ParametersSection(state = state) }
        item { ActionButtons(onApply = onApply, onDismiss = onDismiss) }
        item { Spacer(modifier = Modifier.height(Spacing.XL)) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
    ) {
        Text(
            text = title,
            style = SleepTypography.LabelMedium,
            color = SleepColors.Silver,
            modifier = Modifier.padding(horizontal = Spacing.XS, vertical = Spacing.XXS),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard()
                .padding(Spacing.Medium),
        ) {
            content()
        }
    }
}

@Composable
private fun WinnerSection(state: DiscoveryResultsUiState.Ready) {
    SectionCard(title = "WINNING SHIFT") {
        Text(
            text = state.winningShift.displayName,
            style = SleepTypography.HeadlineMedium,
            color = SleepColors.IndigoGlow,
        )
        Spacer(modifier = Modifier.height(Spacing.XXS))
        val winnerAvg = state.averageRatings[state.winningShift]
        if (winnerAvg != null) {
            Text(
                text = "Average rating: ${"%.1f".format(winnerAvg)} / 5.0",
                style = SleepTypography.BodyMedium,
                color = SleepColors.Silver,
            )
        }
        Spacer(modifier = Modifier.height(Spacing.XS))
        Text(
            text = "This shift produced your best sleep quality over the 21-day programme.",
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
        )
    }
}

@Composable
private fun RatingsSection(state: DiscoveryResultsUiState.Ready) {
    SectionCard(title = "ALL SHIFT RATINGS") {
        val orderedShifts = listOf(ShiftType.LongerLatency, ShiftType.LongerCycles, ShiftType.FewerCycles)
        orderedShifts.forEachIndexed { index, shift ->
            val avg = state.averageRatings[shift]
            ShiftRatingRow(
                shift = shift,
                average = avg,
                isWinner = shift == state.winningShift,
            )
            if (index < orderedShifts.lastIndex) {
                Spacer(modifier = Modifier.height(Spacing.XS))
            }
        }
    }
}

@Composable
private fun ShiftRatingRow(
    shift: ShiftType,
    average: Double?,
    isWinner: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = shift.displayName,
            style = SleepTypography.BodyLarge,
            color = if (isWinner) SleepColors.IndigoGlow else SleepColors.White,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.XS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (average != null) {
                Text(
                    text = "${"%.1f".format(average)} ★",
                    style = SleepTypography.BodyMedium,
                    color = if (isWinner) SleepColors.IndigoGlow else SleepColors.Silver,
                )
            }
            if (isWinner) {
                Text(text = "✓", style = SleepTypography.BodyLarge, color = SleepColors.OptimalGreen)
            }
        }
    }
}

@Composable
private fun ParameterRow(label: String, before: String, after: String, changed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = SleepTypography.BodyLarge, color = SleepColors.White)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(text = before, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
            Text(text = "→", style = SleepTypography.BodyMedium, color = SleepColors.SlateBlue)
            Text(
                text = after,
                style = SleepTypography.BodyMedium,
                color = if (changed) SleepColors.IndigoGlow else SleepColors.Silver,
            )
        }
    }
}

@Composable
private fun ParametersSection(state: DiscoveryResultsUiState.Ready) {
    SectionCard(title = "BEFORE / AFTER") {
        ParameterRow(
            label = "Cycle duration",
            before = "${state.previousCycleDuration} min",
            after = "${state.newCycleDuration} min",
            changed = state.previousCycleDuration != state.newCycleDuration,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        ParameterRow(
            label = "Sleep latency",
            before = "${state.previousSleepLatency} min",
            after = "${state.newSleepLatency} min",
            changed = state.previousSleepLatency != state.newSleepLatency,
        )
        Spacer(modifier = Modifier.height(Spacing.XS))
        ParameterRow(
            label = "Cycle count",
            before = "${state.previousCycleCount}",
            after = "${state.newCycleCount}",
            changed = state.previousCycleCount != state.newCycleCount,
        )
    }
}

@Composable
private fun ActionButtons(
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.XS),
    ) {
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SleepColors.IndigoGlow,
                contentColor = SleepColors.White,
            ),
        ) {
            Text(text = "Apply Results", style = SleepTypography.BodyLarge)
        }
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SleepColors.Silver,
            ),
        ) {
            Text(text = "Dismiss", style = SleepTypography.BodyLarge)
        }
        Text(
            text = "You can return to this screen from Settings at any time.",
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
            modifier = Modifier.padding(top = Spacing.XXS),
        )
    }
}

@Composable
private fun ApplyErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Could Not Apply", style = SleepTypography.HeadlineMedium, color = SleepColors.White)
        },
        text = {
            Text(text = message, style = SleepTypography.BodyMedium, color = SleepColors.Silver)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "OK", color = SleepColors.CyanGlow)
            }
        },
        containerColor = SleepColors.MidnightBlue,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun DiscoveryResultsScreenPreview() {
    NightSkyTheme {
        DiscoveryResultsScreen(
            onApply = {},
            onDismiss = {},
        )
    }
}
