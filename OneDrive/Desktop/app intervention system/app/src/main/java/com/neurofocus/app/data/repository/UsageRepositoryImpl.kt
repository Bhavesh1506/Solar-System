package com.neurofocus.app.data.repository

import com.neurofocus.app.data.local.dao.DailyScreenTime
import com.neurofocus.app.data.local.dao.UsageDao
import com.neurofocus.app.data.local.entity.UsageRecord
import com.neurofocus.app.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UsageRepository backed by Room database.
 * Follows Repository pattern to abstract data source from domain layer.
 */
@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val usageDao: UsageDao
) : UsageRepository {

    override suspend fun insertUsageRecords(records: List<UsageRecord>) =
        usageDao.insertUsageRecords(records)

    override suspend fun getUsageByDate(date: String): List<UsageRecord> =
        usageDao.getUsageByDate(date)

    override fun observeUsageByDate(date: String): Flow<List<UsageRecord>> =
        usageDao.observeUsageByDate(date)

    override suspend fun getTotalScreenTimeMs(date: String): Long =
        usageDao.getTotalScreenTimeMs(date)

    override suspend fun getSocialMediaOpenCount(date: String): Int =
        usageDao.getSocialMediaOpenCount(date)

    override suspend fun getLateNightMinutes(date: String): Int =
        usageDao.getLateNightMinutes(date)

    override suspend fun getAppSwitchCount(date: String): Int =
        usageDao.getAppSwitchCount(date)

    override suspend fun getDailyScreenTime(days: Int): List<DailyScreenTime> =
        usageDao.getDailyScreenTime(days)

    override suspend fun deleteByDate(date: String) =
        usageDao.deleteByDate(date)
}
