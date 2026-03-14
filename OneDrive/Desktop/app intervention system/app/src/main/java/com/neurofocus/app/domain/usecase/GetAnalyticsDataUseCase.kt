package com.neurofocus.app.domain.usecase

import com.neurofocus.app.data.local.dao.DailyScreenTime
import com.neurofocus.app.data.local.dao.DailyInterventionCount
import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import com.neurofocus.app.domain.repository.DopamineScoreRepository
import com.neurofocus.app.domain.repository.InterventionRepository
import com.neurofocus.app.domain.repository.UsageRepository
import javax.inject.Inject

/**
 * Use case for gathering analytics data for the dashboard and analytics screens.
 * Aggregates data from multiple repositories into view-ready models.
 */
class GetAnalyticsDataUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val scoreRepository: DopamineScoreRepository,
    private val interventionRepository: InterventionRepository
) {
    data class AnalyticsData(
        val dailyScreenTime: List<DailyScreenTime>,
        val scoreHistory: List<DopamineScoreRecord>,
        val interventionCounts: List<DailyInterventionCount>
    )

    suspend operator fun invoke(days: Int = 7): AnalyticsData {
        return AnalyticsData(
            dailyScreenTime = usageRepository.getDailyScreenTime(days),
            scoreHistory = scoreRepository.getScoreHistory(days),
            interventionCounts = interventionRepository.getDailyInterventionCounts(days)
        )
    }
}
