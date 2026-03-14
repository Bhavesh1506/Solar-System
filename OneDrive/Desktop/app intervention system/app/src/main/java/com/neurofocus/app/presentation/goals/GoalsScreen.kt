package com.neurofocus.app.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.neurofocus.app.data.local.entity.Goal
import com.neurofocus.app.presentation.theme.*

/**
 * GoalsScreen – Displays goals with XP/Streak/Level progress and
 * add goal functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onStartFocusSession: (Long) -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ─── Header ─────────────────────────────────────────────
        Text(
            text = "Goals & Rewards",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Progress Card (XP, Level, Streak) ──────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStat(
                        icon = "⭐",
                        label = "Level",
                        value = "${state.progress.level}",
                        color = LevelBlue
                    )
                    ProgressStat(
                        icon = "✨",
                        label = "XP",
                        value = "${state.progress.totalXp}",
                        color = XpGold
                    )
                    ProgressStat(
                        icon = "🔥",
                        label = "Streak",
                        value = "${state.progress.currentStreak}d",
                        color = StreakFire
                    )
                    ProgressStat(
                        icon = "🏆",
                        label = "Best",
                        value = "${state.progress.longestStreak}d",
                        color = NeuroTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress to next level
                val xpInLevel = state.progress.totalXp % 500
                val xpProgress = xpInLevel / 500f

                Text(
                    text = "Next Level: ${500 - xpInLevel} XP remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = XpGold,
                    trackColor = XpGold.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Goals List ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FilledTonalButton(
                onClick = { viewModel.toggleAddGoalDialog(true) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Goal")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.goals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎯", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No goals yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Add goals to start earning XP!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onStartSession = { onStartFocusSession(goal.id) },
                        onDelete = { viewModel.deleteGoal(goal) }
                    )
                }
            }
        }

        // ─── Session Result Snackbar ────────────────────────────
        state.sessionResult?.let { result ->
            if (result.xpEarned > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ScoreGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "+${result.xpEarned} XP earned!",
                                fontWeight = FontWeight.Bold,
                                color = ScoreGreen
                            )
                            if (result.levelUp) {
                                Text(
                                    text = "🎊 Level Up! You're now Level ${result.newLevel}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.clearSessionResult() }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
        }
    }

    // ─── Add Goal Dialog ────────────────────────────────────────
    if (state.showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { viewModel.toggleAddGoalDialog(false) },
            onCreate = { name, category, xpReward, targetMinutes ->
                viewModel.createGoal(name, category, xpReward, targetMinutes)
                viewModel.toggleAddGoalDialog(false)
            }
        )
    }
}

// ─── Goal Card ──────────────────────────────────────────────────

@Composable
private fun GoalCard(
    goal: Goal,
    onStartSession: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryEmoji = when (goal.category.lowercase()) {
        "study" -> "📚"
        "gym" -> "💪"
        "coding" -> "💻"
        "reading" -> "📖"
        "meditation" -> "🧘"
        else -> "🎯"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = categoryEmoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = goal.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${goal.category} · ${goal.targetMinutes}min · ${goal.xpReward}XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onStartSession,
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start", style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Progress Stat ──────────────────────────────────────────────

@Composable
private fun ProgressStat(icon: String, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

// ─── Add Goal Dialog ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Study") }
    var xpReward by remember { mutableStateOf("50") }
    var targetMinutes by remember { mutableStateOf("25") }

    val categories = listOf("Study", "Gym", "Coding", "Reading", "Meditation", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = xpReward,
                        onValueChange = { xpReward = it.filter { c -> c.isDigit() } },
                        label = { Text("XP Reward") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetMinutes,
                        onValueChange = { targetMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(
                            name,
                            category,
                            xpReward.toIntOrNull() ?: 50,
                            targetMinutes.toIntOrNull() ?: 25
                        )
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
