package com.neurofocus.app.domain.usecase

import com.neurofocus.app.data.local.entity.FocusSession
import com.neurofocus.app.data.local.entity.UserProgress
import com.neurofocus.app.domain.repository.GoalRepository
import com.neurofocus.app.domain.repository.ProgressRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for recording a completed focus session.
 *
 * Handles the full flow of session completion:
 * 1. Record the focus session
 * 2. Award XP based on the goal's reward
 * 3. Update the user's level (every 500 XP = 1 level)
 * 4. Update streak tracking
 */
class RecordFocusSessionUseCase @Inject constructor(
    private val progressRepository: ProgressRepository,
    private val goalRepository: GoalRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend operator fun invoke(
        goalId: Long,
        durationMinutes: Int,
        completed: Boolean
    ): SessionResult {
        val today = dateFormat.format(Date())
        val goal = goalRepository.getGoalById(goalId)
        val xpEarned = if (completed) (goal?.xpReward ?: 50) else 0

        // Step 1: Record the focus session
        progressRepository.insertSession(
            FocusSession(
                goalId = goalId,
                durationMinutes = durationMinutes,
                xpEarned = xpEarned,
                completed = completed,
                date = today
            )
        )

        if (!completed) {
            return SessionResult(xpEarned = 0, levelUp = false, newLevel = 0, newStreak = 0)
        }

        // Step 2: Update XP and level
        val progress = progressRepository.getProgress() ?: UserProgress()
        val newTotalXp = progress.totalXp + xpEarned
        val newLevel = (newTotalXp / 500) + 1 // Level up every 500 XP
        val levelUp = newLevel > progress.level

        progressRepository.updateXpAndLevel(newTotalXp, newLevel)

        // Step 3: Update streak
        val newStreak = calculateStreak(progress, today)
        val longestStreak = maxOf(newStreak, progress.longestStreak)
        progressRepository.updateStreak(newStreak, longestStreak, today)

        return SessionResult(
            xpEarned = xpEarned,
            levelUp = levelUp,
            newLevel = newLevel,
            newStreak = newStreak
        )
    }

    /**
     * Calculates the current streak length.
     *
     * A streak is maintained if the user completed at least one focus
     * session on consecutive days. If the last active date was yesterday,
     * the streak continues. Otherwise, it resets to 1.
     */
    private fun calculateStreak(progress: UserProgress, today: String): Int {
        if (progress.lastActiveDate.isEmpty()) return 1

        val lastDate = dateFormat.parse(progress.lastActiveDate)
        val todayDate = dateFormat.parse(today)

        if (lastDate == null || todayDate == null) return 1

        val diffMs = todayDate.time - lastDate.time
        val diffDays = (diffMs / (24 * 60 * 60 * 1000)).toInt()

        return when (diffDays) {
            0 -> progress.currentStreak // Same day, streak unchanged
            1 -> progress.currentStreak + 1 // Consecutive day, streak grows
            else -> 1 // Gap detected, streak resets
        }
    }

    data class SessionResult(
        val xpEarned: Int,
        val levelUp: Boolean,
        val newLevel: Int,
        val newStreak: Int
    )
}
