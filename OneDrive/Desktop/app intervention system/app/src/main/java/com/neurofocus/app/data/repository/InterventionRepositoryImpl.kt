package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.DailyInterventionCount
import com.neurofocus.app.data.local.dao.InterventionDao
import com.neurofocus.app.data.local.entity.InterventionRecord
import com.neurofocus.app.domain.repository.InterventionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionRepositoryImpl @Inject constructor(
    private val dao: InterventionDao
) : InterventionRepository {

    override suspend fun insertIntervention(intervention: InterventionRecord) =
        dao.insertIntervention(intervention)

    override suspend fun getInterventionsByDate(date: String) =
        dao.getInterventionsByDate(date)

    override fun observeRecentInterventions(limit: Int): Flow<List<InterventionRecord>> =
        dao.observeRecentInterventions(limit)

    override suspend fun getDailyInterventionCounts(days: Int): List<DailyInterventionCount> =
        dao.getDailyInterventionCounts(days)

    override suspend fun getInterventionCountSince(sinceDate: String) =
        dao.getInterventionCountSince(sinceDate)
}
