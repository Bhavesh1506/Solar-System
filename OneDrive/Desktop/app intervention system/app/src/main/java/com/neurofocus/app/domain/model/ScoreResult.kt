package com.neurofocus.app.domain.model

/**
 * ScoreResult – The output of the Dopamine Score Engine.
 *
 * This class is central to the "Explainable AI" aspect of the system.
 * Rather than just returning a single score, it breaks down every component
 * so the user (and the intervention screen) can explain EXACTLY why the
 * score is what it is.
 *
 * Example explanation:
 *   "Your score is 62.5 because:
 *    - Social media opens: 30 × 2.0 = 60.0  (dominant factor)
 *    - Late-night usage: 5 min × 0.5 = 2.5
 *    - App switching: 0 × 1.5 = 0.0
 *    - Screen time: 0 min × 0.2 = 0.0"
 */
data class ScoreResult(
    /** The total computed dopamine score */
    val totalScore: Float,

    /** Breakdown: socialMediaOpenCount × socialMediaWeight */
    val socialMediaComponent: Float,

    /** Breakdown: lateNightMinutes × lateNightWeight */
    val lateNightComponent: Float,

    /** Breakdown: appSwitchFrequency × appSwitchWeight */
    val appSwitchComponent: Float,

    /** Breakdown: totalScreenTimeMinutes × screenTimeWeight */
    val screenTimeComponent: Float,

    /** Whether this score exceeds the intervention threshold */
    val shouldTriggerIntervention: Boolean,

    /** Human-readable explanation of why the score is high */
    val triggerReason: String,

    /** The raw input values used for the calculation */
    val socialMediaOpenCount: Int,
    val lateNightMinutes: Int,
    val appSwitchFrequency: Int,
    val totalScreenTimeMinutes: Int
)

/**
 * UsageStats – Aggregated usage data for a single day.
 * Intermediate model used between the tracker and the scoring engine.
 */
data class UsageStats(
    val date: String,
    val totalScreenTimeMs: Long,
    val socialMediaOpenCount: Int,
    val lateNightMinutes: Int,
    val appSwitchFrequency: Int,
    val topApps: List<AppUsageSummary> = emptyList()
)

/**
 * Summary of a single app's usage for display purposes.
 */
data class AppUsageSummary(
    val appName: String,
    val packageName: String,
    val timeSpentMs: Long,
    val openCount: Int
)
