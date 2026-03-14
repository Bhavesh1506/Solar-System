package com.neurofocus.app.domain.repository

/**
 * Repository contract for the "Earn Your Time" app blocking system.
 * Handles unlock checking, XP deduction, and unlock creation.
 */
interface AppBlockRepository {

    /** Returns true if the app has an active (non-expired) unlock */
    suspend fun isAppUnlocked(packageName: String): Boolean

    /**
     * Unlocks an app for 15 minutes by deducting XP.
     * @return true if successful (user had enough XP), false otherwise
     */
    suspend fun unlockApp(packageName: String, xpCost: Int = 50, durationMinutes: Int = 15): Boolean

    /** Returns the user's current XP balance */
    suspend fun getXpBalance(): Int

    /** Cleans up expired unlock records */
    suspend fun cleanupExpiredUnlocks()

    /** Returns the list of packages that are flagged as blocked */
    fun getBlockedPackages(): Set<String>
}
