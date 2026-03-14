package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * UsageRecord – Stores per-app usage data collected from UsageStatsManager.
 *
 * Each record represents a single app's usage statistics for a specific date.
 * Fields track time spent, open frequency, late-night usage, and rapid switching
 * behavior — all key signals for the dopamine scoring engine.
 */
@Entity(tableName = "usage_records")
data class UsageRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Package name of the tracked application (e.g., "com.instagram.android") */
    val packageName: String,

    /** Human-readable app name (resolved from package manager) */
    val appName: String,

    /** Total time spent in the app in milliseconds for this date */
    val timeSpentMs: Long,

    /** Number of times the app was opened/foregrounded on this date */
    val openCount: Int,

    /** Minutes of usage between 11 PM and 3 AM (late-night indicator) */
    val lateNightMinutes: Int,

    /** Number of rapid app switches (< 2 seconds between foreground events) */
    val appSwitchCount: Int,

    /** Date string in "yyyy-MM-dd" format for grouping usage by day */
    val date: String,

    /** Timestamp when this record was created/updated */
    val timestamp: Long = System.currentTimeMillis(),

    /** Whether this app is categorized as social media */
    val isSocialMedia: Boolean = false
)
