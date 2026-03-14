package com.neurofocus.app.domain.usecase

import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import com.neurofocus.app.domain.engine.DopamineScoreEngine
import com.neurofocus.app.domain.model.ScoreResult
import com.neurofocus.app.domain.repository.DopamineScoreRepository
import com.neurofocus.app.domain.repository.UsageRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for calculating the dopamine score for a given date.
 *
 * Orchestrates the flow:
 * 1. Fetch aggregated usage stats from the repository
 * 2. Feed them into the DopamineScoreEngine
 * 3. Persist the result
 * 4. Return the ScoreResult for the presentation layer
 */
class CalculateDopamineScoreUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val scoreRepository: DopamineScoreRepository,
    private val scoreEngine: DopamineScoreEngine
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend operator fun invoke(date: String = dateFormat.format(Date())): ScoreResult {
        // Step 1: Gather the four key signals from usage data
        val socialMediaOpens = usageRepository.getSocialMediaOpenCount(date)
        val lateNightMinutes = usageRepository.getLateNightMinutes(date)
        val appSwitchCount = usageRepository.getAppSwitchCount(date)
        val totalScreenTimeMs = usageRepository.getTotalScreenTimeMs(date)
        val totalScreenTimeMinutes = (totalScreenTimeMs / 60000).toInt()

        // Step 2: Calculate the dopamine score
        val result = scoreEngine.calculateScore(
            socialMediaOpenCount = socialMediaOpens,
            lateNightMinutes = lateNightMinutes,
            appSwitchFrequency = appSwitchCount,
            totalScreenTimeMinutes = totalScreenTimeMinutes
        )

        // Step 3: Persist the score record
        scoreRepository.insertScore(
            DopamineScoreRecord(
                score = result.totalScore,
                socialMediaComponent = result.socialMediaComponent,
                lateNightComponent = result.lateNightComponent,
                appSwitchComponent = result.appSwitchComponent,
                screenTimeComponent = result.screenTimeComponent,
                triggeredIntervention = result.shouldTriggerIntervention,
                date = date
            )
        )

        return result
    }
}
