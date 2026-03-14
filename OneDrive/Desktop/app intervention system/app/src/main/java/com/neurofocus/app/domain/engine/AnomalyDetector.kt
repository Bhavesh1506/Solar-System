package com.neurofocus.app.domain.engine

import com.neurofocus.app.domain.repository.DopamineScoreRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * AnomalyDetector – Detects unusual usage patterns using statistical methods.
 *
 * METHODOLOGY:
 * Uses a 7-day moving average as a baseline and flags today's score as anomalous
 * if it exceeds the baseline + 1 standard deviation. This is a simplified form
 * of anomaly detection commonly used in time-series analysis.
 *
 * WHY THIS IS AI-RELEVANT:
 * Statistical anomaly detection is a fundamental technique in machine learning
 * and data science. While this implementation uses basic statistics rather than
 * complex ML models, the principle is the same: identify data points that deviate
 * significantly from established patterns.
 *
 * The 7-day window provides:
 * - Enough data for meaningful statistics
 * - Recency bias (adapts to changing habits)
 * - Robustness against single-day outliers
 */
@Singleton
class AnomalyDetector @Inject constructor(
    private val scoreRepository: DopamineScoreRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Result of anomaly detection analysis.
     *
     * @param isAnomaly Whether today's score is anomalous
     * @param todayScore Today's dopamine score
     * @param baseline 7-day moving average
     * @param standardDeviation Standard deviation of the 7-day window
     * @param deviationFactor How many standard deviations above baseline
     * @param message Human-readable interpretation
     */
    data class AnomalyResult(
        val isAnomaly: Boolean,
        val todayScore: Float,
        val baseline: Float,
        val standardDeviation: Float,
        val deviationFactor: Float,
        val message: String
    )

    /**
     * Analyzes today's score against the 7-day baseline.
     *
     * Algorithm:
     * 1. Retrieve scores from the last 7 days
     * 2. Calculate mean (μ) and standard deviation (σ)
     * 3. Flag as anomaly if today's score > μ + 1σ
     *
     * @param todayScore The current day's dopamine score
     * @return AnomalyResult with detection outcome and statistics
     */
    suspend fun detectAnomaly(todayScore: Float): AnomalyResult {
        // Calculate the date 7 days ago
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = dateFormat.format(calendar.time)

        // Fetch historical scores for baseline calculation
        val historicalScores = scoreRepository.getScoresSince(sevenDaysAgo)

        // Need at least 3 data points for meaningful statistics
        if (historicalScores.size < 3) {
            return AnomalyResult(
                isAnomaly = false,
                todayScore = todayScore,
                baseline = todayScore,
                standardDeviation = 0f,
                deviationFactor = 0f,
                message = "Not enough data for anomaly detection (need 3+ days)."
            )
        }

        // ─── Calculate mean (μ) ─────────────────────────────────
        val mean = historicalScores.average().toFloat()

        // ─── Calculate standard deviation (σ) ───────────────────
        val variance = historicalScores.map { (it - mean) * (it - mean) }
            .average().toFloat()
        val stdDev = sqrt(variance)

        // ─── Detect anomaly: score > μ + 1σ ─────────────────────
        val threshold = mean + stdDev
        val isAnomaly = todayScore > threshold && stdDev > 0
        val deviationFactor = if (stdDev > 0) (todayScore - mean) / stdDev else 0f

        // Generate human-readable interpretation
        val message = when {
            !isAnomaly -> "Your usage today is within your normal patterns."
            deviationFactor > 2 -> "⚠️ Significantly higher than your 7-day average! " +
                    "(${String.format("%.1f", deviationFactor)}σ above baseline)"
            else -> "📈 Your usage is above your 7-day average. " +
                    "(${String.format("%.1f", deviationFactor)}σ above baseline)"
        }

        return AnomalyResult(
            isAnomaly = isAnomaly,
            todayScore = todayScore,
            baseline = mean,
            standardDeviation = stdDev,
            deviationFactor = deviationFactor,
            message = message
        )
    }
}
