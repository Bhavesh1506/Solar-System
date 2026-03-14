package com.neurofocus.app.di

import com.neurofocus.app.data.repository.*
import com.neurofocus.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule – Binds each repository interface to its implementation.
 * Uses @Binds for cleaner Hilt integration compared to @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds
    @Singleton
    abstract fun bindDopamineScoreRepository(impl: DopamineScoreRepositoryImpl): DopamineScoreRepository

    @Binds
    @Singleton
    abstract fun bindInterventionRepository(impl: InterventionRepositoryImpl): InterventionRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindAppBlockRepository(impl: AppBlockRepositoryImpl): AppBlockRepository
}
