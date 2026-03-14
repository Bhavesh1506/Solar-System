package com.neurofocus.app.domain.repository

import com.neurofocus.app.data.local.dao.DailyInterventionCount
import com.neurofocus.app.data.local.entity.InterventionRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for intervention record operations.
 */
interface InterventionRepository {
    suspend fun insertIntervention(intervention: InterventionRecord)
    suspend fun getInterventionsByDate(date: String): List<InterventionRecord>
    fun observeRecentInterventions(limit: Int = 10): Flow<List<InterventionRecord>>
    suspend fun getDailyInterventionCounts(days: Int = 7): List<DailyInterventionCount>
    suspend fun getInterventionCountSince(sinceDate: String): Int
}
