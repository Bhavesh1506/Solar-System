package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.GoalDao
import com.neurofocus.app.data.local.entity.Goal
import com.neurofocus.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {

    override suspend fun insertGoal(goal: Goal) = dao.insertGoal(goal)
    override suspend fun updateGoal(goal: Goal) = dao.updateGoal(goal)
    override suspend fun deleteGoal(goal: Goal) = dao.deleteGoal(goal)
    override fun observeActiveGoals(): Flow<List<Goal>> = dao.observeActiveGoals()
    override fun observeAllGoals(): Flow<List<Goal>> = dao.observeAllGoals()
    override suspend fun getGoalById(goalId: Long) = dao.getGoalById(goalId)
}
