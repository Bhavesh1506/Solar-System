package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.FocusSession
import kotlinx.coroutines.flow.Flow

/**
 * DAO for FocusSession operations.
 * Tracks completed focus sessions for XP/streak calculations and analytics.
 */
@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSession)

    /** Get all sessions for a specific date */
    @Query("SELECT * FROM focus_sessions WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getSessionsByDate(date: String): List<FocusSession>

    /** Get recent sessions as a Flow */
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int = 20): Flow<List<FocusSession>>

    /** Get total XP earned on a specific date */
    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM focus_sessions WHERE date = :date AND completed = 1")
    suspend fun getXpByDate(date: String): Int

    /** Get total completed sessions */
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    suspend fun getTotalCompletedSessions(): Int

    /** Get sessions count per day for streak tracking */
    @Query("""
        SELECT DISTINCT date FROM focus_sessions 
        WHERE completed = 1 
        ORDER BY date DESC 
        LIMIT :days
    """)
    suspend fun getActiveDates(days: Int = 30): List<String>

    /** Get total focus minutes for a date */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE date = :date AND completed = 1")
    suspend fun getTotalFocusMinutes(date: String): Int
}
