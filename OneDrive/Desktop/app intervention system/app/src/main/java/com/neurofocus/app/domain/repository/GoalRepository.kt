package com.neurofocus.app.domain.repository

import com.neurofocus.app.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for goal operations.
 */
interface GoalRepository {
    suspend fun insertGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    fun observeActiveGoals(): Flow<List<Goal>>
    fun observeAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(goalId: Long): Goal?
}
