package com.neurofocus.app.domain.repository

import com.neurofocus.app.data.local.dao.DailyScreenTime
import com.neurofocus.app.data.local.entity.UsageRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for usage data operations.
 * Defines the interface between domain and data layers (Clean Architecture).
 */
interface UsageRepository {
    suspend fun insertUsageRecords(records: List<UsageRecord>)
    suspend fun getUsageByDate(date: String): List<UsageRecord>
    fun observeUsageByDate(date: String): Flow<List<UsageRecord>>
    suspend fun getTotalScreenTimeMs(date: String): Long
    suspend fun getSocialMediaOpenCount(date: String): Int
    suspend fun getLateNightMinutes(date: String): Int
    suspend fun getAppSwitchCount(date: String): Int
    suspend fun getDailyScreenTime(days: Int = 7): List<DailyScreenTime>
    suspend fun deleteByDate(date: String)
}
