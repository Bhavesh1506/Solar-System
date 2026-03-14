package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DopamineScoreRecord – Stores the computed dopamine score and its breakdown.
 *
 * Each record represents a daily snapshot of the user's behavioral score, along
 * with the individual component values that contributed to the final score.
 * This breakdown enables "explainable AI" — the user can see exactly WHY they
 * received a particular score.
 */
@Entity(tableName = "dopamine_scores")
data class DopamineScoreRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** The computed dopamine score for this date */
    val score: Float,

    /** Component: social media open count × weight */
    val socialMediaComponent: Float,

    /** Component: late-night usage minutes × weight */
    val lateNightComponent: Float,

    /** Component: app switch frequency × weight */
    val appSwitchComponent: Float,

    /** Component: total screen time minutes × weight */
    val screenTimeComponent: Float,

    /** Whether this score triggered an intervention */
    val triggeredIntervention: Boolean,

    /** Date string in "yyyy-MM-dd" format */
    val date: String,

    /** Timestamp when the score was computed */
    val timestamp: Long = System.currentTimeMillis()
)
