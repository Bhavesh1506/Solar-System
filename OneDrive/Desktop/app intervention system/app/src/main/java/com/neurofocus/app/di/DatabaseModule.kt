package com.neurofocus.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.neurofocus.app.data.local.NeuroFocusDatabase
import com.neurofocus.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule – Provides the Room database, all DAO instances, and WorkManager.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeuroFocusDatabase {
        return Room.databaseBuilder(
            context,
            NeuroFocusDatabase::class.java,
            NeuroFocusDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideUsageDao(db: NeuroFocusDatabase): UsageDao = db.usageDao()

    @Provides
    fun provideDopamineScoreDao(db: NeuroFocusDatabase): DopamineScoreDao = db.dopamineScoreDao()

    @Provides
    fun provideInterventionDao(db: NeuroFocusDatabase): InterventionDao = db.interventionDao()

    @Provides
    fun provideGoalDao(db: NeuroFocusDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideFocusSessionDao(db: NeuroFocusDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideUserProgressDao(db: NeuroFocusDatabase): UserProgressDao = db.userProgressDao()

    @Provides
    fun provideAppUnlockDao(db: NeuroFocusDatabase): AppUnlockDao = db.appUnlockDao()
}
