package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.AppUnlockDao
import com.neurofocus.app.data.local.dao.UserProgressDao
import com.neurofocus.app.data.local.entity.AppUnlock
import com.neurofocus.app.data.local.entity.UserProgress
import com.neurofocus.app.domain.repository.AppBlockRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AppBlockRepositoryImpl – Implements the "Earn Your Time" economy.
 *
 * Coordinates between AppUnlockDao (unlock tracking) and UserProgressDao
 * (XP balance) to gate app access behind XP payments.
 */
@Singleton
class AppBlockRepositoryImpl @Inject constructor(
    private val appUnlockDao: AppUnlockDao,
    private val userProgressDao: UserProgressDao
) : AppBlockRepository {

    /**
     * Social media and distracting app packages that are blocked by default.
     * The AccessibilityService checks against this list.
     */
    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.twitter.android.lite",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.reddit.frontpage",
        "com.google.android.youtube",
        "com.pinterest",
        "com.discord",
        "com.tumblr"
    )

    override suspend fun isAppUnlocked(packageName: String): Boolean {
        val unlock = appUnlockDao.getActiveUnlock(packageName, System.currentTimeMillis())
        return unlock != null
    }

    override suspend fun unlockApp(packageName: String, xpCost: Int, durationMinutes: Int): Boolean {
        // Step 1: Check XP balance
        val progress = userProgressDao.getProgress() ?: UserProgress()
        if (progress.totalXp < xpCost) {
            return false // Not enough XP
        }

        // Step 2: Deduct XP
        val newXp = progress.totalXp - xpCost
        val newLevel = (newXp / 500) + 1
        userProgressDao.updateXpAndLevel(newXp, newLevel)

        // Step 3: Create unlock record with expiry
        val now = System.currentTimeMillis()
        val expiryMs = now + (durationMinutes * 60 * 1000L)
        appUnlockDao.insertUnlock(
            AppUnlock(
                packageName = packageName,
                unlockTimestamp = now,
                expiryTimestamp = expiryMs,
                xpSpent = xpCost
            )
        )

        return true
    }

    override suspend fun getXpBalance(): Int {
        val progress = userProgressDao.getProgress() ?: UserProgress()
        return progress.totalXp
    }

    override suspend fun cleanupExpiredUnlocks() {
        appUnlockDao.deleteExpired(System.currentTimeMillis())
    }

    override fun getBlockedPackages(): Set<String> = blockedPackages
}
