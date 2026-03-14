package com.neurofocus.app.presentation.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.presentation.theme.*

/**
 * DashboardScreen – Primary screen showing the dopamine score gauge,
 * daily usage stats, and anomaly warnings.
 *
 * Data is loaded ONCE on ViewModel init (app restart). No onResume refresh
 * to avoid overwriting sample/demo data and save battery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToIntervention: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Header ─────────────────────────────────────────────
        Text(
            text = "NeuroFocus",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Dopamine Reallocation Dashboard",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(48.dp))
        } else if (state.errorMessage != null) {
            ErrorCard(message = state.errorMessage!!, onRetry = { viewModel.loadDashboardData() })
        } else {
            state.scoreResult?.let { score ->
                // ─── Score Gauge ─────────────────────────────────
                ScoreGauge(
                    score = score.totalScore,
                    maxScore = 100f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Score interpretation
                Text(
                    text = score.triggerReason,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Anomaly Warning ────────────────────────────
                state.anomalyResult?.let { anomaly ->
                    if (anomaly.isAnomaly) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = ScoreOrange.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = ScoreOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = anomaly.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ScoreOrange
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // ─── Intervention Button ────────────────────────
                if (score.shouldTriggerIntervention) {
                    Button(
                        onClick = onNavigateToIntervention,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScoreRed
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Intervention Suggestions", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ─── Score Breakdown ────────────────────────────
                Text(
                    text = "Score Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakdownCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PhoneAndroid,
                        label = "Social Media",
                        value = "${score.socialMediaOpenCount} opens",
                        contribution = score.socialMediaComponent,
                        color = Color(0xFFE91E63)
                    )
                    BreakdownCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Nightlight,
                        label = "Late Night",
                        value = "${score.lateNightMinutes} min",
                        contribution = score.lateNightComponent,
                        color = Color(0xFF5C6BC0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BreakdownCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SwapHoriz,
                        label = "App Switching",
                        value = "${score.appSwitchFrequency}×",
                        contribution = score.appSwitchComponent,
                        color = Color(0xFFFF9800)
                    )
                    BreakdownCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = "Screen Time",
                        value = "${score.totalScreenTimeMinutes} min",
                        contribution = score.screenTimeComponent,
                        color = Color(0xFF26A69A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Today's Summary ─────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Today's Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow(
                            icon = Icons.Default.ScreenshotMonitor,
                            label = "Total Screen Time",
                            value = formatMinutes(state.totalScreenTimeMinutes.toInt())
                        )
                        SummaryRow(
                            icon = Icons.Default.TouchApp,
                            label = "Social Media Opens",
                            value = "${score.socialMediaOpenCount}"
                        )
                        SummaryRow(
                            icon = Icons.Default.DarkMode,
                            label = "Late Night Usage",
                            value = "${score.lateNightMinutes} min"
                        )
                        SummaryRow(
                            icon = Icons.Default.Shuffle,
                            label = "Rapid Switches",
                            value = "${score.appSwitchFrequency}"
                        )
                    }
                }
            }
        }
    }
}

// ─── Score Gauge Composable ─────────────────────────────────────

@Composable
private fun ScoreGauge(score: Float, maxScore: Float) {
    val animatedScore by animateFloatAsState(
        targetValue = score.coerceIn(0f, maxScore),
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "score"
    )

    val sweepAngle = (animatedScore / maxScore) * 240f
    val gaugeColor = when {
        animatedScore < 25 -> ScoreGreen
        animatedScore < 50 -> ScoreYellow
        animatedScore < 75 -> ScoreOrange
        else -> ScoreRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 18f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc
            drawArc(
                color = gaugeColor.copy(alpha = 0.15f),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Score arc
            drawArc(
                color = gaugeColor,
                startAngle = 150f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Score text in center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.0f", animatedScore),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Text(
                text = "Dopamine Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Helper Composables ─────────────────────────────────────────

@Composable
private fun BreakdownCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    contribution: Float,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "+${String.format("%.1f", contribution)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ScoreRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ScoreRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
