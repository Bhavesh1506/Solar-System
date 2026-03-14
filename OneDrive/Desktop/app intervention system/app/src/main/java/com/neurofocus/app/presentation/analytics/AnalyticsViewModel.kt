package com.neurofocus.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.data.local.dao.DailyInterventionCount
import com.neurofocus.app.data.local.dao.DailyScreenTime
import com.neurofocus.app.data.local.entity.DopamineScoreRecord
import com.neurofocus.app.domain.usecase.GetAnalyticsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AnalyticsViewModel – Loads 7-day analytics data for charts and trends.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsDataUseCase: GetAnalyticsDataUseCase
) : ViewModel() {

    data class AnalyticsState(
        val isLoading: Boolean = true,
        val scoreHistory: List<DopamineScoreRecord> = emptyList(),
        val dailyScreenTime: List<DailyScreenTime> = emptyList(),
        val interventionCounts: List<DailyInterventionCount> = emptyList(),
        val errorMessage: String? = null
    )

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics(days: Int = 7) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val data = getAnalyticsDataUseCase(days)
                _state.value = AnalyticsState(
                    isLoading = false,
                    scoreHistory = data.scoreHistory,
                    dailyScreenTime = data.dailyScreenTime,
                    interventionCounts = data.interventionCounts
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to load analytics: ${e.message}"
                )
            }
        }
    }
}
