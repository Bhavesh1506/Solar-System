package com.neurofocus.app.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.data.local.dao.DailyInterventionCount
import com.neurofocus.app.data.local.dao.DailyScreenTime
import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import com.neurofocus.app.presentation.theme.*

/**
 * AnalyticsScreen – Displays 7-day trends for dopamine score,
 * screen time, and intervention frequency using custom Canvas charts.
 */
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "7-Day Usage Trends",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(48.dp))
            }
        } else if (state.scoreHistory.isEmpty()) {
            EmptyStateCard()
        } else {
            // ─── Score Trend Chart ──────────────────────────────
            ChartCard(title = "Dopamine Score Trend") {
                ScoreTrendChart(
                    scores = state.scoreHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Screen Time Chart ──────────────────────────────
            ChartCard(title = "Daily Screen Time") {
                ScreenTimeBarChart(
                    data = state.dailyScreenTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Intervention Frequency ─────────────────────────
            ChartCard(title = "Intervention Frequency") {
                InterventionBarChart(
                    data = state.interventionCounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Summary Stats ──────────────────────────────────
            SummaryStatsRow(state)
        }
    }
}

// ─── Chart Components ───────────────────────────────────────────

@Composable
private fun ScoreTrendChart(scores: List<DopamineScoreRecord>, modifier: Modifier = Modifier) {
    val lineColor = NeuroMediumPurple
    val fillColor = NeuroLightPurple.copy(alpha = 0.2f)
    val thresholdColor = ScoreRed.copy(alpha = 0.4f)

    Canvas(modifier = modifier.padding(start = 32.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)) {
        if (scores.isEmpty()) return@Canvas

        val maxScore = (scores.maxOf { it.score }).coerceAtLeast(60f)
        val stepX = size.width / (scores.size - 1).coerceAtLeast(1)

        // Draw threshold line at 50
        val thresholdY = size.height * (1 - 50f / maxScore)
        drawLine(
            color = thresholdColor,
            start = Offset(0f, thresholdY),
            end = Offset(size.width, thresholdY),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f), 0f
            )
        )

        // Draw score line
        val path = Path()
        scores.forEachIndexed { i, record ->
            val x = i * stepX
            val y = size.height * (1 - record.score / maxScore)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Draw data points
        scores.forEachIndexed { i, record ->
            val x = i * stepX
            val y = size.height * (1 - record.score / maxScore)
            drawCircle(color = lineColor, radius = 5f, center = Offset(x, y))
            drawCircle(color = Color.White, radius = 3f, center = Offset(x, y))
        }

        // Draw date labels
        scores.forEachIndexed { i, record ->
            val x = i * stepX
            drawContext.canvas.nativeCanvas.drawText(
                record.date.takeLast(5), // "MM-DD"
                x,
                size.height + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun ScreenTimeBarChart(data: List<DailyScreenTime>, modifier: Modifier = Modifier) {
    val barColor = NeuroTeal

    Canvas(modifier = modifier.padding(start = 32.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)) {
        if (data.isEmpty()) return@Canvas

        val maxMinutes = data.maxOf { (it.timeSpentMs / 60000f) }
            .coerceAtLeast(60f)
        val barWidth = (size.width / data.size) * 0.6f
        val gap = (size.width / data.size) * 0.4f

        data.forEachIndexed { i, item ->
            val minutes = item.timeSpentMs / 60000f
            val barHeight = (minutes / maxMinutes) * size.height
            val x = i * (barWidth + gap) + gap / 2

            // Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Value label
            drawContext.canvas.nativeCanvas.drawText(
                "${item.timeSpentMs / 60000f}m",
                x + barWidth / 2,
                size.height - barHeight - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            // Date label
            drawContext.canvas.nativeCanvas.drawText(
                item.date.takeLast(5),
                x + barWidth / 2,
                size.height + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun InterventionBarChart(data: List<DailyInterventionCount>, modifier: Modifier = Modifier) {
    val barColor = ScoreOrange

    Canvas(modifier = modifier.padding(start = 32.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)) {
        if (data.isEmpty()) return@Canvas

        val maxCount = (data.maxOf { it.count }).coerceAtLeast(3).toFloat()
        val barWidth = (size.width / data.size) * 0.6f
        val gap = (size.width / data.size) * 0.4f

        data.forEachIndexed { i, item ->
            val barHeight = (item.count / maxCount) * size.height
            val x = i * (barWidth + gap) + gap / 2

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${item.count}",
                x + barWidth / 2,
                size.height - barHeight - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )

            drawContext.canvas.nativeCanvas.drawText(
                item.date.takeLast(5),
                x + barWidth / 2,
                size.height + 16.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// ─── Helper Composables ─────────────────────────────────────────

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SummaryStatsRow(state: AnalyticsViewModel.AnalyticsState) {
    val avgScore = if (state.scoreHistory.isNotEmpty()) {
        state.scoreHistory.map { it.score }.average()
    } else 0.0

    val totalInterventions = state.interventionCounts.sumOf { it.count }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Avg Score",
            value = String.format("%.1f", avgScore),
            color = NeuroMediumPurple
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Interventions",
            value = "$totalInterventions",
            color = ScoreOrange
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Days Tracked",
            value = "${state.scoreHistory.size}",
            color = NeuroTeal
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No analytics data yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Go to Settings → Generate Sample Data to see charts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
