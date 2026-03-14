package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * DAO for UserProgress operations.
 * Manages the singleton user progress record for XP/level/streak.
 */
@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserProgress)

    /** Get the current user progress (singleton) */
    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getProgress(): UserProgress?

    /** Observe user progress reactively */
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun observeProgress(): Flow<UserProgress?>

    /** Update XP and level */
    @Query("UPDATE user_progress SET totalXp = :totalXp, level = :level, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateXpAndLevel(totalXp: Int, level: Int, timestamp: Long = System.currentTimeMillis())

    /** Update streak data */
    @Query("""
        UPDATE user_progress 
        SET currentStreak = :currentStreak, longestStreak = :longestStreak, 
            lastActiveDate = :lastActiveDate, lastUpdated = :timestamp 
        WHERE id = 1
    """)
    suspend fun updateStreak(
        currentStreak: Int,
        longestStreak: Int,
        lastActiveDate: String,
        timestamp: Long = System.currentTimeMillis()
    )
}
