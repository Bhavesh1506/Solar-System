package com.neurofocus.app.data.usage

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.neurofocus.app.domain.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * UsageTrackingWorker – Periodic background worker that collects usage data.
 *
 * Runs every 30 minutes via WorkManager to:
 * 1. Collect current usage data from UsageStatsManager
 * 2. Store it in the Room database
 *
 * Uses @HiltWorker for dependency injection of the tracker and repository.
 */
@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageStatsTracker: UsageStatsTracker,
    private val usageRepository: UsageRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Step 1: Collect today's usage data from the system
            val usageRecords = usageStatsTracker.collectTodayUsage()

            if (usageRecords.isNotEmpty()) {
                // Step 2: Get today's date string for cleanup
                val today = usageRecords.first().date

                // Step 3: Delete old records for today (will be replaced)
                usageRepository.deleteByDate(today)

                // Step 4: Insert fresh usage data
                usageRepository.insertUsageRecords(usageRecords)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry on failure (WorkManager handles backoff)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "usage_tracking_worker"

        /**
         * Creates a periodic work request that runs every 30 minutes.
         * Uses KEEP existing policy to avoid duplicate workers.
         */
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // Don't track when battery is low
                .build()

            return PeriodicWorkRequestBuilder<UsageTrackingWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}
