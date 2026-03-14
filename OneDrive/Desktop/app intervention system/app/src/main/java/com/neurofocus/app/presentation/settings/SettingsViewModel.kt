package com.neurofocus.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.data.local.entity.*
import com.neurofocus.app.domain.engine.DopamineScoreEngine
import com.neurofocus.app.domain.model.ScoringWeights
import com.neurofocus.app.domain.repository.DopamineScoreRepository
import com.neurofocus.app.domain.repository.GoalRepository
import com.neurofocus.app.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

/**
 * SettingsViewModel – Manages scoring weights and sample data generation.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val scoreEngine: DopamineScoreEngine,
    private val usageRepository: UsageRepository,
    private val scoreRepository: DopamineScoreRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    data class SettingsState(
        val weights: ScoringWeights = ScoringWeights(),
        val sampleDataGenerated: Boolean = false,
        val isGenerating: Boolean = false
    )

    private val _state = MutableStateFlow(SettingsState(weights = scoreEngine.getCurrentWeights()))
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateWeights(newWeights: ScoringWeights) {
        scoreEngine.updateWeights(newWeights)
        _state.value = _state.value.copy(weights = newWeights)
    }

    /**
     * Generates realistic sample usage data for the past 7 days.
     * This is essential for demo purposes since UsageStatsManager
     * requires real device usage history.
     */
    fun generateSampleData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val random = Random(42) // Fixed seed for reproducible demo data

            // Social media apps for sample data
            val sampleApps = listOf(
                "com.instagram.android" to "Instagram",
                "com.twitter.android" to "Twitter",
                "com.google.android.youtube" to "YouTube",
                "com.whatsapp" to "WhatsApp",
                "com.reddit.frontpage" to "Reddit",
                "com.google.android.gm" to "Gmail",
                "com.android.chrome" to "Chrome",
                "com.spotify.music" to "Spotify"
            )

            // Generate 7 days of data
            for (dayOffset in 6 downTo 0) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val date = dateFormat.format(calendar.time)

                // Delete existing data for this date
                usageRepository.deleteByDate(date)

                // Generate usage records for 4-6 random apps
                val numApps = random.nextInt(4, 7)
                val selectedApps = sampleApps.shuffled(random).take(numApps)
                val records = selectedApps.map { (pkg, name) ->
                    val isSocial = pkg in setOf(
                        "com.instagram.android", "com.twitter.android",
                        "com.google.android.youtube", "com.whatsapp", "com.reddit.frontpage"
                    )
                    UsageRecord(
                        packageName = pkg,
                        appName = name,
                        timeSpentMs = random.nextLong(60_000, 3_600_000), // 1-60 min
                        openCount = random.nextInt(2, 30),
                        lateNightMinutes = if (random.nextFloat() > 0.6f) random.nextInt(5, 25) else 0,
                        appSwitchCount = random.nextInt(0, 20),
                        date = date,
                        isSocialMedia = isSocial
                    )
                }

                usageRepository.insertUsageRecords(records)

                // Compute and store score for this day
                val socialOpens = records.filter { it.isSocialMedia }.sumOf { it.openCount }
                val lateNightMins = records.sumOf { it.lateNightMinutes }
                val switches = records.sumOf { it.appSwitchCount }
                val totalScreenMin = (records.sumOf { it.timeSpentMs } / 60000).toInt()

                val result = scoreEngine.calculateScore(socialOpens, lateNightMins, switches, totalScreenMin)
                scoreRepository.insertScore(
                    DopamineScoreRecord(
                        score = result.totalScore,
                        socialMediaComponent = result.socialMediaComponent,
                        lateNightComponent = result.lateNightComponent,
                        appSwitchComponent = result.appSwitchComponent,
                        screenTimeComponent = result.screenTimeComponent,
                        triggeredIntervention = result.shouldTriggerIntervention,
                        date = date
                    )
                )
            }

            // Create sample goals if none exist
            val existingGoals = goalRepository.observeAllGoals().first()
            if (existingGoals.isEmpty()) {
                goalRepository.insertGoal(Goal(name = "Read for 30 minutes", category = "Reading", xpReward = 50, targetMinutes = 30))
                goalRepository.insertGoal(Goal(name = "Study DSA", category = "Study", xpReward = 75, targetMinutes = 45))
                goalRepository.insertGoal(Goal(name = "Workout", category = "Gym", xpReward = 60, targetMinutes = 30))
                goalRepository.insertGoal(Goal(name = "Practice LeetCode", category = "Coding", xpReward = 80, targetMinutes = 40))
            }

            _state.value = _state.value.copy(
                isGenerating = false,
                sampleDataGenerated = true
            )
        }
    }
}
