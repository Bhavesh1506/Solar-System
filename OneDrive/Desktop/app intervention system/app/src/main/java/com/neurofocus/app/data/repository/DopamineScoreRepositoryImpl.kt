package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.DopamineScoreDao
import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import com.neurofocus.app.domain.repository.DopamineScoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DopamineScoreRepositoryImpl @Inject constructor(
    private val dao: DopamineScoreDao
) : DopamineScoreRepository {

    override suspend fun insertScore(score: DopamineScoreRecord) = dao.insertScore(score)

    override suspend fun getScoreByDate(date: String) = dao.getScoreByDate(date)

    override fun observeScoreByDate(date: String): Flow<DopamineScoreRecord?> =
        dao.observeScoreByDate(date)

    override suspend fun getScoreHistory(days: Int) = dao.getScoreHistory(days)

    override fun observeScoreHistory(days: Int): Flow<List<DopamineScoreRecord>> =
        dao.observeScoreHistory(days)

    override suspend fun getAverageScore(sinceDate: String) = dao.getAverageScore(sinceDate)

    override suspend fun getScoresSince(sinceDate: String) = dao.getScoresSince(sinceDate)
}
