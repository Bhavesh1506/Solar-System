package com.neurofocus.app.presentation.goals

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.presentation.theme.*
import kotlinx.coroutines.delay

/**
 * FocusTimerScreen – Countdown timer for focus sessions with XP reward.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    goalId: Long,
    onBack: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val goal = state.goals.find { it.id == goalId }
    val totalSeconds = (goal?.targetMinutes ?: 25) * 60

    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    // Timer tick
    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds <= 0 && isRunning) {
            isRunning = false
            isCompleted = true
            viewModel.recordFocusSession(goalId, goal?.targetMinutes ?: 25, completed = true)
        }
    }

    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "timer_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Top Bar ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isRunning && !isCompleted) {
                    // Abandon session
                    viewModel.recordFocusSession(goalId, (totalSeconds - remainingSeconds) / 60, completed = false)
                }
                onBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Focus Session",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goal info
        goal?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🎯 ${it.name} · ${it.xpReward} XP",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // ─── Timer Ring ─────────────────────────────────────────
        val ringColor = when {
            isCompleted -> ScoreGreen
            progress < 0.25f -> ScoreOrange
            else -> NeuroMediumPurple
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val strokeWidth = 14f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // Background ring
                drawArc(
                    color = ringColor.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress ring
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Timer text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isCompleted) {
                    Text(text = "✅", fontSize = 48.sp)
                    Text(
                        text = "Complete!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScoreGreen
                    )
                } else {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = ringColor
                    )
                    Text(
                        text = if (isRunning) "Stay focused!" else "Ready?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // ─── Session Result ─────────────────────────────────────
        if (isCompleted) {
            state.sessionResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ScoreGreen.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎉 Great job!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "+${result.xpEarned} XP earned", color = XpGold, fontSize = 18.sp)
                        if (result.levelUp) {
                            Text(text = "🎊 Level Up → Level ${result.newLevel}!", color = LevelBlue)
                        }
                        Text(text = "🔥 Streak: ${result.newStreak} days", color = StreakFire)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    viewModel.clearSessionResult()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        } else {
            // ─── Controls ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRunning) {
                    OutlinedButton(
                        onClick = { isRunning = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause")
                    }
                } else {
                    Button(
                        onClick = { isRunning = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (remainingSeconds < totalSeconds) "Resume" else "Start")
                    }
                }

                if (!isRunning && remainingSeconds < totalSeconds) {
                    OutlinedButton(
                        onClick = {
                            remainingSeconds = totalSeconds
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
