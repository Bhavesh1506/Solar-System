package com.neurofocus.app.domain.model

/**
 * ScoringWeights – Configurable weights for the Dopamine Score Engine.
 *
 * These weights determine how much each behavioral signal contributes
 * to the overall dopamine score. By making them configurable, users and
 * researchers can tune the system to different contexts.
 *
 * DEFAULT WEIGHTS RATIONALE:
 * - socialMediaWeight (2.0): Social media opens are a strong addiction indicator.
 *   Each open represents a dopamine-seeking "check" behavior.
 * - lateNightWeight (0.5): Late-night phone usage correlates with compulsive use
 *   and sleep disruption. Measured in minutes, so lower weight per unit.
 * - appSwitchWeight (1.5): Rapid switching indicates low attention span and
 *   dopamine-driven browsing behavior.
 * - screenTimeWeight (0.2): Total screen time is a broad indicator. Lower weight
 *   because some screen time is productive.
 *
 * THRESHOLD:
 * - interventionThreshold (50.0): Score above this triggers an intervention.
 *   Calibrated so a typical "heavy phone day" (~3h screen time, 30 social media
 *   opens, 15min late-night, 20 app switches) would score around 50-60.
 */
data class ScoringWeights(
    val socialMediaWeight: Float = 2.0f,
    val lateNightWeight: Float = 0.5f,
    val appSwitchWeight: Float = 1.5f,
    val screenTimeWeight: Float = 0.2f,
    val interventionThreshold: Float = 50.0f
)
