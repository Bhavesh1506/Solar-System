package com.neurofocus.app.domain.usecase

import com.neurofocus.app.data.local.entity.Goal
import com.neurofocus.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing user-defined goals (CRUD operations).
 */
class ManageGoalsUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    fun observeActiveGoals(): Flow<List<Goal>> = goalRepository.observeActiveGoals()

    fun observeAllGoals(): Flow<List<Goal>> = goalRepository.observeAllGoals()

    suspend fun createGoal(name: String, category: String, xpReward: Int, targetMinutes: Int): Long {
        return goalRepository.insertGoal(
            Goal(
                name = name,
                category = category,
                xpReward = xpReward,
                targetMinutes = targetMinutes
            )
        )
    }

    suspend fun updateGoal(goal: Goal) = goalRepository.updateGoal(goal)

    suspend fun deleteGoal(goal: Goal) = goalRepository.deleteGoal(goal)

    suspend fun getGoalById(goalId: Long) = goalRepository.getGoalById(goalId)
}
