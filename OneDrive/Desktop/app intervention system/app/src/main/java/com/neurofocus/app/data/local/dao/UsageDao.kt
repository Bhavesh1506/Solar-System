package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.UsageRecord
import kotlinx.coroutines.flow.Flow

/**
 * DAO for UsageRecord operations.
 * Provides queries for usage monitoring, scoring, and analytics.
 */
@Dao
interface UsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: UsageRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecords(records: List<UsageRecord>)

    /** Get all usage records for a specific date */
    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY timeSpentMs DESC")
    suspend fun getUsageByDate(date: String): List<UsageRecord>

    /** Get all usage records for a specific date as a Flow (reactive) */
    @Query("SELECT * FROM usage_records WHERE date = :date ORDER BY timeSpentMs DESC")
    fun observeUsageByDate(date: String): Flow<List<UsageRecord>>

    /** Get total screen time in ms for a specific date */
    @Query("SELECT COALESCE(SUM(timeSpentMs), 0) FROM usage_records WHERE date = :date")
    suspend fun getTotalScreenTimeMs(date: String): Long

    /** Get total social media open count for a date */
    @Query("SELECT COALESCE(SUM(openCount), 0) FROM usage_records WHERE date = :date AND isSocialMedia = 1")
    suspend fun getSocialMediaOpenCount(date: String): Int

    /** Get total late-night usage minutes for a date */
    @Query("SELECT COALESCE(SUM(lateNightMinutes), 0) FROM usage_records WHERE date = :date")
    suspend fun getLateNightMinutes(date: String): Int

    /** Get total app switch count for a date */
    @Query("SELECT COALESCE(SUM(appSwitchCount), 0) FROM usage_records WHERE date = :date")
    suspend fun getAppSwitchCount(date: String): Int

    /** Get daily total screen time for the last N days (for charts) */
    @Query("""
        SELECT date, SUM(timeSpentMs) as timeSpentMs 
        FROM usage_records 
        GROUP BY date 
        ORDER BY date DESC 
        LIMIT :days
    """)
    suspend fun getDailyScreenTime(days: Int = 7): List<DailyScreenTime>

    /** Delete records older than a specified date */
    @Query("DELETE FROM usage_records WHERE date < :beforeDate")
    suspend fun deleteOldRecords(beforeDate: String)

    @Query("DELETE FROM usage_records WHERE date = :date")
    suspend fun deleteByDate(date: String)
}

/** Projection class for daily screen time aggregation */
data class DailyScreenTime(
    val date: String,
    val timeSpentMs: Long
)
