package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * UserProgress – Tracks the user's gamification progress.
 *
 * Maintains XP totals, current level, streak data, and last active date.
 * Only one row should exist in this table (singleton pattern via ID = 1).
 *
 * Level Progression Formula:
 *   level = floor(totalXp / 500) + 1
 *   xpForNextLevel = 500 - (totalXp % 500)
 */
@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val id: Int = 1, // Singleton row

    /** Total XP accumulated across all focus sessions */
    val totalXp: Int = 0,

    /** Current level (derived from totalXp) */
    val level: Int = 1,

    /** Current consecutive day streak */
    val currentStreak: Int = 0,

    /** Longest streak ever achieved */
    val longestStreak: Int = 0,

    /** Total number of completed focus sessions */
    val totalSessions: Int = 0,

    /** Date of last completed focus session in "yyyy-MM-dd" format */
    val lastActiveDate: String = "",

    /** Timestamp of last update */
    val lastUpdated: Long = System.currentTimeMillis()
)
