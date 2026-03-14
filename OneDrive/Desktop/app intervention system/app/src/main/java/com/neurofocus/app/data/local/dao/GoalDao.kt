package com.neurofocus.app.data.local.dao

import androidx.room.*
import com.neurofocus.app.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Goal operations.
 * Supports CRUD for user-defined productive goals.
 */
@Dao
interface GoalDao {

    @Insert
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    /** Get all active goals */
    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActiveGoals(): Flow<List<Goal>>

    /** Get all goals (including inactive) */
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun observeAllGoals(): Flow<List<Goal>>

    /** Get a specific goal by ID */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): Goal?

    /** Get goal count */
    @Query("SELECT COUNT(*) FROM goals WHERE isActive = 1")
    suspend fun getActiveGoalCount(): Int
}
