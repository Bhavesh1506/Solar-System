package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FocusSession – Records a completed focus session for a specific goal.
 *
 * When a user completes a focus timer session, the duration, associated goal,
 * and XP earned are captured here. This data feeds into streak tracking and
 * the XP/level progression system.
 */
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** ID of the goal this session was for (foreign key to goals table) */
    val goalId: Long,

    /** Actual duration of the focus session in minutes */
    val durationMinutes: Int,

    /** XP earned for this session */
    val xpEarned: Int,

    /** Whether the session was fully completed (vs. abandoned early) */
    val completed: Boolean,

    /** Date string in "yyyy-MM-dd" format */
    val date: String,

    /** Timestamp when the session was completed */
    val timestamp: Long = System.currentTimeMillis()
)
