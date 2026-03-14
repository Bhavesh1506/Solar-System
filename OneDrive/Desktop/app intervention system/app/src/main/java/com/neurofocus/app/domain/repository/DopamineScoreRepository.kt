package com.neurofocus.app.domain.repository

import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for dopamine score operations.
 */
interface DopamineScoreRepository {
    suspend fun insertScore(score: DopamineScoreRecord)
    suspend fun getScoreByDate(date: String): DopamineScoreRecord?
    fun observeScoreByDate(date: String): Flow<DopamineScoreRecord?>
    suspend fun getScoreHistory(days: Int = 7): List<DopamineScoreRecord>
    fun observeScoreHistory(days: Int = 7): Flow<List<DopamineScoreRecord>>
    suspend fun getAverageScore(sinceDate: String): Float
    suspend fun getScoresSince(sinceDate: String): List<Float>
}
