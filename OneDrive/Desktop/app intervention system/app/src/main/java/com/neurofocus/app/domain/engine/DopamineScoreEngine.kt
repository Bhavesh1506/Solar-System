package com.neurofocus.app.domain.engine

import com.neurofocus.app.domain.model.ScoreResult
import com.neurofocus.app.domain.model.ScoringWeights
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DopamineScoreEngine – Core AI Component (Explainable Rule-Based System)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * WHY THIS QUALIFIES AS AN AI-BASED SYSTEM:
 * ──────────────────────────────────────────
 * This engine implements a rule-based expert system, which is a well-established
 * branch of Artificial Intelligence dating back to the 1970s (MYCIN, DENDRAL).
 * Rule-based systems are a form of "Good Old-Fashioned AI" (GOFAI) that use
 * human-codified knowledge to make decisions.
 *
 * Key AI characteristics:
 * 1. PATTERN RECOGNITION: The engine identifies addictive behavior patterns
 *    by analyzing multiple behavioral signals simultaneously.
 * 2. DECISION MAKING: Based on the combined score, it autonomously decides
 *    when to trigger an intervention — mimicking expert judgment.
 * 3. EXPLAINABILITY: Unlike black-box ML models, every decision can be
 *    traced back to specific input factors with exact weights, making this
 *    an "Explainable AI" (XAI) system.
 * 4. CONFIGURABLE KNOWLEDGE BASE: The weights serve as a tunable knowledge
 *    base, similar to how expert systems allow domain experts to adjust rules.
 * 5. BEHAVIORAL SCORING: The weighted linear combination is a simplified form
 *    of the scoring functions used in recommendation systems and behavioral
 *    analytics platforms.
 *
 * SCORING FORMULA:
 * ────────────────
 *   dopamineScore = (socialMediaOpenCount × socialMediaWeight)
 *                 + (lateNightUsageMinutes × lateNightWeight)
 *                 + (appSwitchFrequency × appSwitchWeight)
 *                 + (totalScreenTimeMinutes × screenTimeWeight)
 *
 * Each component targets a specific addictive behavior:
 * - Social media opens → Dopamine-seeking "check" behavior
 * - Late-night usage → Compulsive use / sleep disruption
 * - App switching → Shortened attention span / restlessness
 * - Screen time → Overall device dependency
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Singleton
class DopamineScoreEngine @Inject constructor() {

    /** Current scoring weights (configurable at runtime via Settings) */
    private var weights = ScoringWeights()

    /**
     * Calculates the dopamine score based on the four behavioral signals.
     *
     * @param socialMediaOpenCount Number of times social media apps were opened today
     * @param lateNightMinutes Minutes of phone usage between 11 PM and 3 AM
     * @param appSwitchFrequency Number of rapid (< 2s) app switches detected
     * @param totalScreenTimeMinutes Total screen-on time in minutes
     * @return ScoreResult with total score, component breakdown, and intervention decision
     */
    fun calculateScore(
        socialMediaOpenCount: Int,
        lateNightMinutes: Int,
        appSwitchFrequency: Int,
        totalScreenTimeMinutes: Int
    ): ScoreResult {
        // ─── Step 1: Calculate individual components ─────────────
        // Each component = raw value × corresponding weight
        // This makes every part of the score traceable (Explainable AI)
        val socialMediaComponent = socialMediaOpenCount * weights.socialMediaWeight
        val lateNightComponent = lateNightMinutes * weights.lateNightWeight
        val appSwitchComponent = appSwitchFrequency * weights.appSwitchWeight
        val screenTimeComponent = totalScreenTimeMinutes * weights.screenTimeWeight

        // ─── Step 2: Sum all components to get final score ───────
        val totalScore = socialMediaComponent +
                lateNightComponent +
                appSwitchComponent +
                screenTimeComponent

        // ─── Step 3: Determine if intervention should be triggered ───
        val shouldTrigger = totalScore >= weights.interventionThreshold

        // ─── Step 4: Generate human-readable explanation ─────────
        // This is the "Explainable" part of our AI system:
        // Users can see exactly which factors contributed most
        val triggerReason = buildTriggerReason(
            totalScore, socialMediaComponent, lateNightComponent,
            appSwitchComponent, screenTimeComponent
        )

        return ScoreResult(
            totalScore = totalScore,
            socialMediaComponent = socialMediaComponent,
            lateNightComponent = lateNightComponent,
            appSwitchComponent = appSwitchComponent,
            screenTimeComponent = screenTimeComponent,
            shouldTriggerIntervention = shouldTrigger,
            triggerReason = triggerReason,
            socialMediaOpenCount = socialMediaOpenCount,
            lateNightMinutes = lateNightMinutes,
            appSwitchFrequency = appSwitchFrequency,
            totalScreenTimeMinutes = totalScreenTimeMinutes
        )
    }

    /**
     * Builds a human-readable explanation of the score.
     * Identifies the dominant factor and explains why the score is concerning.
     */
    private fun buildTriggerReason(
        totalScore: Float,
        socialMedia: Float,
        lateNight: Float,
        appSwitch: Float,
        screenTime: Float
    ): String {
        if (totalScore < weights.interventionThreshold) {
            return "Your usage patterns are within healthy limits today."
        }

        val components = listOf(
            "Social media browsing" to socialMedia,
            "Late-night phone usage" to lateNight,
            "Rapid app switching" to appSwitch,
            "Extended screen time" to screenTime
        )

        // Find the dominant factor (highest contributing component)
        val dominant = components.maxByOrNull { it.second }!!
        val significantFactors = components.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(2)
            .map { it.first }

        return buildString {
            append("⚠️ Your dopamine score is ${String.format("%.1f", totalScore)}. ")
            append("Main factor: ${dominant.first}. ")
            if (significantFactors.size > 1) {
                append("Also elevated: ${significantFactors.last()}.")
            }
        }
    }

    /**
     * Updates the scoring weights at runtime.
     * Called from the Settings screen when user adjusts weights.
     */
    fun updateWeights(newWeights: ScoringWeights) {
        weights = newWeights
    }

    /** Returns the current weights for display in Settings */
    fun getCurrentWeights(): ScoringWeights = weights
}
