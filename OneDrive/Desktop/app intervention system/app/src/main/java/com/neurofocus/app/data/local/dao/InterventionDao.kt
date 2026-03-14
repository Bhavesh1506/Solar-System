package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.InterventionRecord
import kotlinx.coroutines.flow.Flow

/**
 * DAO for InterventionRecord operations.
 * Tracks intervention history for analytics and effectiveness measurement.
 */
@Dao
interface InterventionDao {

    @Insert
    suspend fun insertIntervention(intervention: InterventionRecord)

    /** Get all interventions for a specific date */
    @Query("SELECT * FROM interventions WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getInterventionsByDate(date: String): List<InterventionRecord>

    /** Get recent interventions as a Flow (for dashboard) */
    @Query("SELECT * FROM interventions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentInterventions(limit: Int = 10): Flow<List<InterventionRecord>>

    /** Get intervention count per day for the last N days (for charts) */
    @Query("""
        SELECT date, COUNT(*) as count 
        FROM interventions 
        GROUP BY date 
        ORDER BY date DESC 
        LIMIT :days
    """)
    suspend fun getDailyInterventionCounts(days: Int = 7): List<DailyInterventionCount>

    /** Get total interventions since a date */
    @Query("SELECT COUNT(*) FROM interventions WHERE date >= :sinceDate")
    suspend fun getInterventionCountSince(sinceDate: String): Int

    /** Get accepted intervention count (user engaged with suggestion) */
    @Query("SELECT COUNT(*) FROM interventions WHERE userAction = 'accepted' OR userAction = 'completed'")
    suspend fun getAcceptedInterventionCount(): Int
}

/** Projection class for daily intervention count */
data class DailyInterventionCount(
    val date: String,
    val count: Int
)
