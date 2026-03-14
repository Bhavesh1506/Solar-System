package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * InterventionRecord – Logs every intervention event triggered by the dopamine engine.
 *
 * Tracks what triggered the intervention, the score at the time, what suggestion was
 * shown, and whether the user accepted or dismissed it. This data feeds into the
 * analytics dashboard to show intervention frequency and effectiveness.
 */
@Entity(tableName = "interventions")
data class InterventionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** The dopamine score that triggered this intervention */
    val triggerScore: Float,

    /** Human-readable reason why the intervention was triggered */
    val triggerReason: String,

    /** The suggestion shown to the user (e.g., "10-min focus session") */
    val suggestion: String,

    /** User's response: "accepted", "dismissed", or "completed" */
    val userAction: String,

    /** Date string in "yyyy-MM-dd" format */
    val date: String,

    /** Timestamp when the intervention occurred */
    val timestamp: Long = System.currentTimeMillis()
)
