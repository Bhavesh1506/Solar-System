package com.neurofocus.app.domain.usecase

import com.neurofocus.app.data.local.entity.InterventionRecord
import com.neurofocus.app.domain.model.ScoreResult
import com.neurofocus.app.domain.repository.InterventionRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for triggering and logging interventions.
 *
 * When the dopamine score exceeds the threshold, this use case:
 * 1. Generates appropriate suggestions based on the dominant factor
 * 2. Logs the intervention event for analytics
 * 3. Returns the suggestion to show on the intervention screen
 */
class TriggerInterventionUseCase @Inject constructor(
    private val interventionRepository: InterventionRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class InterventionSuggestion(
        val title: String,
        val description: String,
        val actionType: String // "focus_session", "breathing", "micro_goal"
    )

    /**
     * Generates contextual suggestions based on the score breakdown.
     * Different dominant factors lead to different intervention types.
     */
    fun generateSuggestions(scoreResult: ScoreResult): List<InterventionSuggestion> {
        val suggestions = mutableListOf<InterventionSuggestion>()

        // Always suggest a focus session (core redirection mechanism)
        suggestions.add(
            InterventionSuggestion(
                title = "🎯 10-Minute Focus Session",
                description = "Redirect your energy toward a productive goal. " +
                        "Choose a task and earn XP!",
                actionType = "focus_session"
            )
        )

        // Breathing exercise (especially good for late-night/high-stress usage)
        suggestions.add(
            InterventionSuggestion(
                title = "🧘 Breathing Exercise",
                description = "Take 5 deep breaths. Inhale for 4 seconds, " +
                        "hold for 7, exhale for 8. Reset your mind.",
                actionType = "breathing"
            )
        )

        // Micro-goal based on dominant factor
        val microGoal = when {
            scoreResult.socialMediaComponent > scoreResult.screenTimeComponent ->
                InterventionSuggestion(
                    title = "📱 Social Media Detox Micro-Goal",
                    description = "Put your phone face-down for 15 minutes. " +
                            "You've opened social media ${scoreResult.socialMediaOpenCount} times today.",
                    actionType = "micro_goal"
                )
            scoreResult.lateNightComponent > 0 ->
                InterventionSuggestion(
                    title = "🌙 Sleep Hygiene Micro-Goal",
                    description = "It's late! Set your phone aside and read a " +
                            "physical book or do light stretching instead.",
                    actionType = "micro_goal"
                )
            else ->
                InterventionSuggestion(
                    title = "✅ Complete One Micro-Goal",
                    description = "Do something small but productive: organize " +
                            "your desk, write 3 sentences, or plan tomorrow.",
                    actionType = "micro_goal"
                )
        }
        suggestions.add(microGoal)

        return suggestions
    }

    /**
     * Logs the intervention event for analytics tracking.
     */
    suspend fun logIntervention(
        scoreResult: ScoreResult,
        suggestion: String,
        userAction: String
    ) {
        interventionRepository.insertIntervention(
            InterventionRecord(
                triggerScore = scoreResult.totalScore,
                triggerReason = scoreResult.triggerReason,
                suggestion = suggestion,
                userAction = userAction,
                date = dateFormat.format(Date())
            )
        )
    }
}
