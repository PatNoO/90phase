package com.example.a90phase.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a90phase.domain.entities.ConsistencyScore
import com.example.a90phase.domain.entities.PatternInsight
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.presentation.R
import com.example.a90phase.presentation.components.SectionHeader
import com.example.a90phase.presentation.components.SleepLogCard
import com.example.a90phase.presentation.theme.NightSkyTheme
import com.example.a90phase.presentation.theme.SleepColors
import com.example.a90phase.presentation.theme.SleepShapes
import com.example.a90phase.presentation.theme.SleepTypography
import com.example.a90phase.presentation.theme.Spacing
import com.example.a90phase.presentation.theme.StarFieldBackground
import com.example.a90phase.presentation.theme.glassCard
import com.example.a90phase.presentation.util.localizedName
import com.example.a90phase.presentation.viewmodels.HistoryUiState
import com.example.a90phase.presentation.viewmodels.HistoryViewModel

private enum class HistoryPeriod { WEEK, MONTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToLogDetail: (String) -> Unit = {},
) {
    val vmState by viewModel.uiState.collectAsStateWithLifecycle()
    var period by remember { mutableStateOf(HistoryPeriod.WEEK) }
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        viewModel.errors.collect { messageRes ->
            snackbarHostState.showSnackbar(resources.getString(messageRes))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StarFieldBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { HistoryTopBar(period = period, onPeriodChange = { period = it }) },
            floatingActionButton = { HistoryFab(visible = vmState is HistoryUiState.Content) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            when (val state = vmState) {
                is HistoryUiState.Loading -> HistoryLoadingState(Modifier.padding(innerPadding))
                is HistoryUiState.Empty -> HistoryEmptyState(Modifier.padding(innerPadding))
                is HistoryUiState.Content -> HistoryContent(
                    logs = state.logs,
                    period = period,
                    insights = state.insights,
                    consistencyScore = state.consistencyScore,
                    onDismissInsight = viewModel::onDismissInsight,
                    onLogClick = onNavigateToLogDetail,
                    modifier = Modifier.padding(innerPadding),
                )
                is HistoryUiState.Error -> Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(state.messageRes),
                        color = SleepColors.ErrorRed,
                        style = SleepTypography.BodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFab(visible: Boolean) {
    val addLogDescription = stringResource(R.string.history_add_log)
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(200)) + fadeIn(tween(200)),
        exit = scaleOut(tween(150)) + fadeOut(tween(150)),
    ) {
        FloatingActionButton(
            onClick = {},
            containerColor = SleepColors.CyanGlow,
            contentColor = SleepColors.DeepSpace,
            modifier = Modifier.semantics { contentDescription = addLogDescription },
        ) {
            Text(text = "+", style = SleepTypography.HeadlineMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(period: HistoryPeriod, onPeriodChange: (HistoryPeriod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.history_title), style = SleepTypography.HeadlineMedium, color = SleepColors.White)
        },
        actions = {
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = stringResource(
                            if (period == HistoryPeriod.WEEK) R.string.history_period_week else R.string.history_period_month,
                        ),
                        style = SleepTypography.BodyMedium,
                        color = SleepColors.CyanGlow,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = SleepColors.MidnightBlue,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.history_period_week_item),
                                color = SleepColors.White,
                                style = SleepTypography.BodyMedium,
                            )
                        },
                        onClick = { onPeriodChange(HistoryPeriod.WEEK); expanded = false },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.history_period_month_item),
                                color = SleepColors.White,
                                style = SleepTypography.BodyMedium,
                            )
                        },
                        onClick = { onPeriodChange(HistoryPeriod.MONTH); expanded = false },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
    )
}

@Composable
private fun HistoryContent(
    logs: List<SleepLog>,
    period: HistoryPeriod,
    insights: List<PatternInsight>,
    consistencyScore: ConsistencyScore?,
    onDismissInsight: (String) -> Unit,
    onLogClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.XL),
    ) {
        item {
            Spacer(Modifier.height(Spacing.Medium))
            WeeklySummaryCard(logs = logs, consistencyScore = consistencyScore)
        }
        item {
            Spacer(Modifier.height(Spacing.Medium))
            SleepQualityChart(logs = logs, period = period)
        }
        items(insights) { insight ->
            Spacer(Modifier.height(Spacing.Medium))
            PatternInsightCard(
                message = insight.message,
                onDismiss = { onDismissInsight(insight.id) },
            )
        }
        item {
            Spacer(Modifier.height(Spacing.Medium))
            SectionHeader(title = stringResource(R.string.history_section_title))
        }
        items(logs) { log ->
            SleepLogCard(log = log, onClick = { onLogClick(log.id) })
        }
    }
}

@Composable
private fun WeeklySummaryCard(logs: List<SleepLog>, consistencyScore: ConsistencyScore?) {
    val avgRating = logs.mapNotNull { it.qualityRating }.average().takeIf { !it.isNaN() } ?: 0.0
    // localizedName(), not DayOfWeek.toString() — the enum name is always English.
    val bestDay = logs.maxByOrNull { it.qualityRating ?: 0 }?.date?.dayOfWeek?.localizedName()
    val avgMinutes = if (logs.isEmpty()) 0 else logs.map { it.sleepDurationMinutes }.average().toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
            .glassCard()
            .padding(Spacing.Medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularQualityIndicator(rating = avgRating.toFloat())
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.history_avg_duration, avgMinutes / 60, avgMinutes % 60),
                    style = SleepTypography.HeadlineMedium,
                    color = SleepColors.White,
                )
                Spacer(Modifier.height(Spacing.XXS))
                Text(
                    text = stringResource(R.string.history_best_day, bestDay ?: stringResource(R.string.history_best_day_none)),
                    style = SleepTypography.BodyMedium,
                    color = SleepColors.Silver,
                )
                if (consistencyScore != null) {
                    Spacer(Modifier.height(Spacing.XXS))
                    ConsistencyScoreRow(score = consistencyScore)
                }
            }
        }
    }
}

@Composable
private fun ConsistencyScoreRow(score: ConsistencyScore) {
    val labelColor = when (score.label) {
        ConsistencyScore.Label.HIGH -> SleepColors.OptimalGreen
        ConsistencyScore.Label.MEDIUM -> SleepColors.GoodAmber
        ConsistencyScore.Label.LOW -> SleepColors.ErrorRed
    }
    val labelText = stringResource(
        when (score.label) {
            ConsistencyScore.Label.HIGH -> R.string.history_consistency_high
            ConsistencyScore.Label.MEDIUM -> R.string.history_consistency_medium
            ConsistencyScore.Label.LOW -> R.string.history_consistency_low
        },
    )
    val description = stringResource(R.string.history_consistency_description, labelText, score.percentage)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // spacedBy, not a trailing space in the resource — AAPT strips resource whitespace.
        horizontalArrangement = Arrangement.spacedBy(Spacing.XXS, Alignment.End),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = description
        },
    ) {
        Text(
            text = stringResource(R.string.history_consistency_prefix),
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = stringResource(R.string.history_consistency_value, labelText, score.percentage),
            style = SleepTypography.BodyMedium,
            color = labelColor,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun CircularQualityIndicator(rating: Float) {
    val sweepAngle = (rating / 5f).coerceIn(0f, 1f) * 360f
    val ratingDescription = stringResource(R.string.history_avg_rating_description, "%.1f".format(rating))
    Box(
        modifier = Modifier.size(80.dp).clearAndSetSemantics {
            contentDescription = ratingDescription
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = SleepColors.GlassSurface,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize,
            )
            if (sweepAngle > 0f) {
                drawArc(
                    color = SleepColors.CyanGlow,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%.1f".format(rating), style = SleepTypography.HeadlineLarge, color = SleepColors.White)
            Text("/ 5", style = SleepTypography.LabelMedium, color = SleepColors.Silver)
        }
    }
}

@Composable
private fun SleepQualityChart(logs: List<SleepLog>, period: HistoryPeriod) {
    val displayLogs = logs.take(if (period == HistoryPeriod.WEEK) 7 else 30).reversed()
    val periodLabel = stringResource(
        if (period == HistoryPeriod.WEEK) R.string.history_period_past_week else R.string.history_period_past_month,
    )
    val avgRating = displayLogs.mapNotNull { it.qualityRating }.average().takeIf { !it.isNaN() }
    val chartDesc = if (avgRating != null) {
        stringResource(R.string.history_quality_chart_description_data, periodLabel, "%.1f".format(avgRating))
    } else {
        stringResource(R.string.history_quality_chart_description_empty, periodLabel)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
            .glassCard()
            .semantics { contentDescription = chartDesc }
            .padding(Spacing.Medium),
    ) {
        Column {
            Text(stringResource(R.string.history_quality_chart_title), style = SleepTypography.LabelMedium, color = SleepColors.Silver)
            Spacer(Modifier.height(Spacing.XS))
            QualityDotCanvas(logs = displayLogs, modifier = Modifier.fillMaxWidth().height(100.dp))
        }
    }
}

@Composable
private fun QualityDotCanvas(logs: List<SleepLog>, modifier: Modifier = Modifier) {
    val gridColor = SleepColors.White.copy(alpha = 0.05f)
    val optimalColor = SleepColors.OptimalGreen
    val cyanColor = SleepColors.CyanGlow
    val amberColor = SleepColors.GoodAmber
    val errorColor = SleepColors.ErrorRed
    Canvas(modifier = modifier) {
        val cols = logs.size.coerceAtLeast(7)
        val colWidth = size.width / cols
        for (row in 1..4) {
            val y = size.height * row / 5f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        logs.forEachIndexed { idx, log ->
            val rating = log.qualityRating ?: return@forEachIndexed
            val x = colWidth * idx + colWidth / 2f
            val y = size.height - (rating / 5f) * size.height
            val dotColor = when {
                rating >= 4 -> optimalColor
                rating >= 3 -> cyanColor
                rating >= 2 -> amberColor
                else -> errorColor
            }
            drawCircle(color = dotColor.copy(alpha = 0.25f), radius = 10.dp.toPx(), center = Offset(x, y))
            drawCircle(color = dotColor, radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun PatternInsightCard(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium)
            .glassCard()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = SleepTypography.BodyMedium,
            color = SleepColors.Silver,
            modifier = Modifier.weight(1f),
        )
        val dismissDescription = stringResource(R.string.history_dismiss_insight)
        TextButton(onClick = onDismiss) {
            Text(
                text = "✕",
                style = SleepTypography.BodyMedium,
                color = SleepColors.SlateBlue,
                modifier = Modifier.clearAndSetSemantics { contentDescription = dismissDescription },
            )
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.history_no_logs_title),
                style = SleepTypography.HeadlineMedium,
                color = SleepColors.Silver,
            )
            Spacer(Modifier.height(Spacing.XS))
            Text(
                text = stringResource(R.string.history_no_logs_subtitle),
                style = SleepTypography.BodyMedium,
                color = SleepColors.SlateBlue,
            )
        }
    }
}

@Composable
private fun HistoryLoadingState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = Spacing.Medium)) {
        Spacer(Modifier.height(Spacing.Medium))
        ShimmerBox(Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(Spacing.Medium))
        ShimmerBox(Modifier.fillMaxWidth().height(140.dp))
        Spacer(Modifier.height(Spacing.Medium))
        repeat(3) {
            ShimmerBox(Modifier.fillMaxWidth().height(80.dp))
            Spacer(Modifier.height(Spacing.XS))
        }
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "ShimmerOffset",
    )
    Box(
        modifier = modifier
            .clip(SleepShapes.Medium)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SleepColors.GlassSurface,
                            SleepColors.GlassSurface.copy(alpha = 0.3f),
                            SleepColors.GlassSurface,
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 300f, 0f),
                    ),
                )
            },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun HistoryScreenPreview() {
    NightSkyTheme {
        HistoryScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120, widthDp = 640, heightDp = 360)
@Composable
internal fun HistoryScreenLandscapePreview() {
    NightSkyTheme {
        HistoryScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun HistoryEmptyStatePreview() {
    NightSkyTheme {
        HistoryEmptyState()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
internal fun HistoryLoadingStatePreview() {
    NightSkyTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StarFieldBackground()
            CircularProgressIndicator(
                color = SleepColors.CyanGlow,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
