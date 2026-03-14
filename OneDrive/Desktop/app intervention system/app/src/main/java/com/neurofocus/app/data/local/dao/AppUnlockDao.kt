package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.AppUnlock

/**
 * DAO for AppUnlock operations – the "Earn Your Time" unlock tracking.
 */
@Dao
interface AppUnlockDao {

    /**
     * Checks if the given app is currently unlocked (i.e., an active
     * unlock record exists whose expiry is still in the future).
     */
    @Query("""
        SELECT * FROM app_unlocks 
        WHERE packageName = :packageName AND expiryTimestamp > :currentTime 
        ORDER BY expiryTimestamp DESC 
        LIMIT 1
    """)
    suspend fun getActiveUnlock(packageName: String, currentTime: Long = System.currentTimeMillis()): AppUnlock?

    /** Insert a new unlock record (when user pays XP) */
    @Insert
    suspend fun insertUnlock(unlock: AppUnlock)

    /** Clean up expired unlock records */
    @Query("DELETE FROM app_unlocks WHERE expiryTimestamp < :currentTime")
    suspend fun deleteExpired(currentTime: Long = System.currentTimeMillis())

    /** Get all active unlocks (for display purposes) */
    @Query("SELECT * FROM app_unlocks WHERE expiryTimestamp > :currentTime")
    suspend fun getAllActiveUnlocks(currentTime: Long = System.currentTimeMillis()): List<AppUnlock>
}
