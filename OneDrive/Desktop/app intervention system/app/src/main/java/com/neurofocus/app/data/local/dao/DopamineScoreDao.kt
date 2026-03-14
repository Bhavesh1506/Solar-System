package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import kotlinx.coroutines.flow.Flow

/**
 * DAO for DopamineScoreRecord operations.
 * Provides queries for score history, trends, and analytics.
 */
@Dao
interface DopamineScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: DopamineScoreRecord)

    /** Get the latest score for today */
    @Query("SELECT * FROM dopamine_scores WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    suspend fun getScoreByDate(date: String): DopamineScoreRecord?

    /** Get the latest score for today as a Flow */
    @Query("SELECT * FROM dopamine_scores WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun observeScoreByDate(date: String): Flow<DopamineScoreRecord?>

    /** Get score history for the last N days (for trend charts) */
    @Query("SELECT * FROM dopamine_scores GROUP BY date ORDER BY date DESC LIMIT :days")
    suspend fun getScoreHistory(days: Int = 7): List<DopamineScoreRecord>

    /** Get score history as a reactive Flow */
    @Query("SELECT * FROM dopamine_scores GROUP BY date ORDER BY date DESC LIMIT :days")
    fun observeScoreHistory(days: Int = 7): Flow<List<DopamineScoreRecord>>

    /** Get average score over the last N days (for anomaly detection baseline) */
    @Query("SELECT COALESCE(AVG(score), 0) FROM dopamine_scores WHERE date >= :sinceDate")
    suspend fun getAverageScore(sinceDate: String): Float

    /** Get all scores since a date (for standard deviation calculation) */
    @Query("SELECT score FROM dopamine_scores WHERE date >= :sinceDate ORDER BY date ASC")
    suspend fun getScoresSince(sinceDate: String): List<Float>

    /** Get count of interventions triggered in a date range */
    @Query("SELECT COUNT(*) FROM dopamine_scores WHERE triggeredIntervention = 1 AND date >= :sinceDate")
    suspend fun getInterventionCount(sinceDate: String): Int
}
