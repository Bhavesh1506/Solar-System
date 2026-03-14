package com.neurofocus.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.domain.engine.AnomalyDetector
import com.neurofocus.app.domain.model.ScoreResult
import com.neurofocus.app.domain.repository.UsageRepository
import com.neurofocus.app.domain.usecase.CalculateDopamineScoreUseCase
import com.neurofocus.app.domain.usecase.TriggerInterventionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * DashboardViewModel – Drives the main dashboard screen.
 *
 * Fetches data ONCE during init{} (i.e., on app restart / ViewModel creation).
 * This avoids:
 *  - Overwriting sample data via onResume re-fetch
 *  - Unnecessary battery drain from background polling
 *  - Complexity of lifecycle-aware refresh mechanisms
 *
 * Data is exposed via a single StateFlow that the UI collects from.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calculateScoreUseCase: CalculateDopamineScoreUseCase,
    private val anomalyDetector: AnomalyDetector,
    private val triggerInterventionUseCase: TriggerInterventionUseCase,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class DashboardState(
        val isLoading: Boolean = true,
        val scoreResult: ScoreResult? = null,
        val anomalyResult: AnomalyDetector.AnomalyResult? = null,
        val suggestions: List<TriggerInterventionUseCase.InterventionSuggestion> = emptyList(),
        val totalScreenTimeMinutes: Long = 0,
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // ─── Single fetch on ViewModel creation ─────────────────────
    init {
        loadDashboardData()
    }

    /**
     * Loads all dashboard data asynchronously: score, anomaly detection,
     * intervention suggestions, and screen time. Called ONLY from init{}.
     *
     * Can also be invoked manually via a pull-to-refresh or retry button.
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val today = dateFormat.format(Date())

                // Calculate today's dopamine score
                val scoreResult = calculateScoreUseCase(today)

                // Run anomaly detection
                val anomalyResult = anomalyDetector.detectAnomaly(scoreResult.totalScore)

                // Generate suggestions if intervention threshold exceeded
                val suggestions = if (scoreResult.shouldTriggerIntervention) {
                    triggerInterventionUseCase.generateSuggestions(scoreResult)
                } else emptyList()

                // Get total screen time
                val screenTimeMs = usageRepository.getTotalScreenTimeMs(today)

                _state.value = DashboardState(
                    isLoading = false,
                    scoreResult = scoreResult,
                    anomalyResult = anomalyResult,
                    suggestions = suggestions,
                    totalScreenTimeMinutes = screenTimeMs / 60000
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to load dashboard: ${e.message}"
                )
            }
        }
    }
}
