package com.neurofocus.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neurofocus.app.data.local.dao.*
import com.neurofocus.app.data.local.entity.*

/**
 * NeuroFocusDatabase – Central Room database for the application.
 *
 * Contains all entities for usage tracking, dopamine scoring,
 * intervention logging, goals, focus sessions, and user progress.
 * Version is set to 1 for the initial release.
 */
@Database(
    entities = [
        UsageRecord::class,
        DopamineScoreRecord::class,
        InterventionRecord::class,
        Goal::class,
        FocusSession::class,
        UserProgress::class,
        AppUnlock::class
    ],
    version = 2,
    exportSchema = true
)
abstract class NeuroFocusDatabase : RoomDatabase() {

    abstract fun usageDao(): UsageDao
    abstract fun dopamineScoreDao(): DopamineScoreDao
    abstract fun interventionDao(): InterventionDao
    abstract fun goalDao(): GoalDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun appUnlockDao(): AppUnlockDao

    companion object {
        const val DATABASE_NAME = "neurofocus_db"
    }
}
