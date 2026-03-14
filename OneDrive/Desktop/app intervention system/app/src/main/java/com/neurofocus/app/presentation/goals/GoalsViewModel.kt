package com.neurofocus.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.data.local.entity.Goal
import com.neurofocus.app.data.local.entity.UserProgress
import com.neurofocus.app.domain.repository.ProgressRepository
import com.neurofocus.app.domain.usecase.ManageGoalsUseCase
import com.neurofocus.app.domain.usecase.RecordFocusSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GoalsViewModel – Manages goals, focus sessions, XP, streaks, and levels.
 */
@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val manageGoalsUseCase: ManageGoalsUseCase,
    private val recordFocusSessionUseCase: RecordFocusSessionUseCase,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    data class GoalsState(
        val goals: List<Goal> = emptyList(),
        val progress: UserProgress = UserProgress(),
        val sessionResult: RecordFocusSessionUseCase.SessionResult? = null,
        val showAddGoalDialog: Boolean = false,
        val isTimerRunning: Boolean = false,
        val timerGoalId: Long = 0,
        val timerRemainingSeconds: Int = 0
    )

    private val _state = MutableStateFlow(GoalsState())
    val state: StateFlow<GoalsState> = _state.asStateFlow()

    init {
        // Observe goals reactively
        viewModelScope.launch {
            manageGoalsUseCase.observeActiveGoals().collect { goals ->
                _state.value = _state.value.copy(goals = goals)
            }
        }

        // Observe user progress reactively
        viewModelScope.launch {
            progressRepository.observeProgress().collect { progress ->
                _state.value = _state.value.copy(progress = progress ?: UserProgress())
            }
        }
    }

    fun createGoal(name: String, category: String, xpReward: Int, targetMinutes: Int) {
        viewModelScope.launch {
            manageGoalsUseCase.createGoal(name, category, xpReward, targetMinutes)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            manageGoalsUseCase.deleteGoal(goal)
        }
    }

    fun recordFocusSession(goalId: Long, durationMinutes: Int, completed: Boolean) {
        viewModelScope.launch {
            val result = recordFocusSessionUseCase(goalId, durationMinutes, completed)
            _state.value = _state.value.copy(sessionResult = result)
        }
    }

    fun clearSessionResult() {
        _state.value = _state.value.copy(sessionResult = null)
    }

    fun toggleAddGoalDialog(show: Boolean) {
        _state.value = _state.value.copy(showAddGoalDialog = show)
    }
}
