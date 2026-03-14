package com.neurofocus.app.presentation.intervention

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.domain.usecase.TriggerInterventionUseCase
import com.neurofocus.app.presentation.dashboard.DashboardViewModel
import com.neurofocus.app.presentation.theme.*

/**
 * InterventionScreen – Full-screen overlay shown when the dopamine score
 * exceeds the intervention threshold.
 *
 * Displays the score, trigger reason, and contextual suggestions that
 * redirect the user toward productive activities.
 */
@Composable
fun InterventionScreen(
    onDismiss: () -> Unit,
    onStartFocusSession: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ScoreRed.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ─── Warning Icon ───────────────────────────────────────
        Text(text = "⚠️", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Intervention Alert",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ScoreRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ─── Score Display ──────────────────────────────────────
        state.scoreResult?.let { score ->
            Text(
                text = String.format("%.0f", score.totalScore),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = ScoreRed
            )
            Text(
                text = "Dopamine Score",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trigger reason
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ScoreRed.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = score.triggerReason,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Suggestions ────────────────────────────────────
            Text(
                text = "What you can do instead:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            state.suggestions.forEach { suggestion ->
                SuggestionCard(
                    suggestion = suggestion,
                    onAccept = {
                        when (suggestion.actionType) {
                            "focus_session" -> {
                                // Navigate to focus session with the first available goal
                                onStartFocusSession(0)
                            }
                            else -> onDismiss()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Dismiss Button ─────────────────────────────────────
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("I'll continue anyway")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your usage has been logged for analytics.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuggestionCard(
    suggestion: TriggerInterventionUseCase.InterventionSuggestion,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = onAccept,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Let's do this")
            }
        }
    }
}
