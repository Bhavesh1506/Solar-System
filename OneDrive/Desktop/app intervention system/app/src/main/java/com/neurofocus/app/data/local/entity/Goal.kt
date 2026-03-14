package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal – Represents a user-defined productive goal.
 *
 * Users can create goals in categories like Study, Gym, Coding, Reading, etc.
 * Each goal has an XP reward for completion, enabling gamification of productive
 * activities as a counter-balance to addictive app usage.
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Name of the goal (e.g., "Read for 30 minutes") */
    val name: String,

    /** Category of the goal (e.g., "Study", "Gym", "Coding", "Reading") */
    val category: String,

    /** XP reward for completing a focus session toward this goal */
    val xpReward: Int = 50,

    /** Target duration for a focus session in minutes */
    val targetMinutes: Int = 25,

    /** Whether this goal is currently active */
    val isActive: Boolean = true,

    /** Timestamp when the goal was created */
    val createdAt: Long = System.currentTimeMillis()
)
