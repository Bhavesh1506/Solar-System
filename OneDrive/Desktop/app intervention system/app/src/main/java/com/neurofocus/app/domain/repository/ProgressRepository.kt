package com.neurofocus.app.domain.repository

import com.neurofocus.app.data.local.entity.FocusSession
import com.neurofocus.app.data.local.entity.UserProgress
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for focus sessions and user progress operations.
 */
interface ProgressRepository {
    // Focus Session operations
    suspend fun insertSession(session: FocusSession)
    suspend fun getSessionsByDate(date: String): List<FocusSession>
    fun observeRecentSessions(limit: Int = 20): Flow<List<FocusSession>>
    suspend fun getActiveDates(days: Int = 30): List<String>
    suspend fun getTotalFocusMinutes(date: String): Int

    // User Progress operations
    suspend fun getProgress(): UserProgress?
    fun observeProgress(): Flow<UserProgress?>
    suspend fun upsertProgress(progress: UserProgress)
    suspend fun updateXpAndLevel(totalXp: Int, level: Int)
    suspend fun updateStreak(currentStreak: Int, longestStreak: Int, lastActiveDate: String)
}
