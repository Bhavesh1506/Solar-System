package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.FocusSessionDao
import com.neurofocus.app.data.local.dao.UserProgressDao
import com.neurofocus.app.data.local.entity.FocusSession
import com.neurofocus.app.data.local.entity.UserProgress
import com.neurofocus.app.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val userProgressDao: UserProgressDao
) : ProgressRepository {

    // ─── Focus Session operations ───────────────────────────────
    override suspend fun insertSession(session: FocusSession) =
        focusSessionDao.insertSession(session)

    override suspend fun getSessionsByDate(date: String) =
        focusSessionDao.getSessionsByDate(date)

    override fun observeRecentSessions(limit: Int): Flow<List<FocusSession>> =
        focusSessionDao.observeRecentSessions(limit)

    override suspend fun getActiveDates(days: Int) =
        focusSessionDao.getActiveDates(days)

    override suspend fun getTotalFocusMinutes(date: String) =
        focusSessionDao.getTotalFocusMinutes(date)

    // ─── User Progress operations ───────────────────────────────
    override suspend fun getProgress() = userProgressDao.getProgress()

    override fun observeProgress(): Flow<UserProgress?> = userProgressDao.observeProgress()

    override suspend fun upsertProgress(progress: UserProgress) =
        userProgressDao.upsertProgress(progress)

    override suspend fun updateXpAndLevel(totalXp: Int, level: Int) =
        userProgressDao.updateXpAndLevel(totalXp, level)

    override suspend fun updateStreak(currentStreak: Int, longestStreak: Int, lastActiveDate: String) =
        userProgressDao.updateStreak(currentStreak, longestStreak, lastActiveDate)
}
