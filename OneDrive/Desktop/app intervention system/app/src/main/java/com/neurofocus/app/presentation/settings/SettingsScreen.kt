package com.neurofocus.app.presentation.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.domain.model.ScoringWeights
import com.neurofocus.app.presentation.theme.*

/**
 * SettingsScreen – Configure scoring weights, intervention threshold,
 * and generate sample data for demo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var weights by remember { mutableStateOf(state.weights) }

    // Sync when state updates
    LaunchedEffect(state.weights) {
        weights = state.weights
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Scoring Weights ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scoring Weights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Adjust how each behavioral signal contributes to the dopamine score",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                WeightSlider(
                    label = "📱 Social Media",
                    value = weights.socialMediaWeight,
                    range = 0f..5f,
                    onValueChange = {
                        weights = weights.copy(socialMediaWeight = it)
                    }
                )

                WeightSlider(
                    label = "🌙 Late Night Usage",
                    value = weights.lateNightWeight,
                    range = 0f..3f,
                    onValueChange = {
                        weights = weights.copy(lateNightWeight = it)
                    }
                )

                WeightSlider(
                    label = "🔄 App Switching",
                    value = weights.appSwitchWeight,
                    range = 0f..5f,
                    onValueChange = {
                        weights = weights.copy(appSwitchWeight = it)
                    }
                )

                WeightSlider(
                    label = "⏱️ Screen Time",
                    value = weights.screenTimeWeight,
                    range = 0f..2f,
                    onValueChange = {
                        weights = weights.copy(screenTimeWeight = it)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Intervention Threshold
                Text(
                    text = "⚠️ Intervention Threshold: ${String.format("%.0f", weights.interventionThreshold)}",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = weights.interventionThreshold,
                    onValueChange = { weights = weights.copy(interventionThreshold = it) },
                    valueRange = 20f..100f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Button(
                    onClick = { viewModel.updateWeights(weights) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Weights")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Sample Data Generator ──────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DataUsage, contentDescription = null, tint = NeuroTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Generate 7 days of sample usage data for demonstration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.generateSampleData() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = NeuroTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Sample Data")
                    }
                }

                if (state.sampleDataGenerated) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ScoreGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ScoreGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sample data generated! Check Dashboard & Analytics.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScoreGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── About Section ──────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About NeuroFocus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NeuroFocus is an AI-based Dopamine Reallocation System " +
                            "that monitors smartphone usage patterns and uses a " +
                            "rule-based expert system to detect addictive behavior. " +
                            "When the dopamine score exceeds the threshold, it triggers " +
                            "interventions that redirect users toward productive goals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version 1.0.0 · Clean Architecture · MVVM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeightSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}
