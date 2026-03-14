package com.neurofocus.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AppUnlock – Tracks temporarily unlocked apps in the "Earn Your Time" system.
 *
 * When a user pays XP to unlock a blocked app, a record is created here with
 * a 15-minute expiry window. The AccessibilityService checks this table to
 * determine if a blocked app should be allowed through.
 */
@Entity(tableName = "app_unlocks")
data class AppUnlock(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Package name of the unlocked app (e.g., "com.instagram.android") */
    val packageName: String,

    /** Timestamp when the app was unlocked */
    val unlockTimestamp: Long = System.currentTimeMillis(),

    /** Timestamp when the unlock expires (unlockTimestamp + 15 minutes) */
    val expiryTimestamp: Long,

    /** XP spent for this unlock */
    val xpSpent: Int = 50
)
