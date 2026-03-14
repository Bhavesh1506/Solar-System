package com.neurofocus.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.neurofocus.app.data.usage.UsageTrackingWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * NeuroFocusApp – Application class with Hilt DI and WorkManager setup.
 *
 * Implements Configuration.Provider to use HiltWorkerFactory for injecting
 * dependencies into WorkManager workers (like UsageTrackingWorker).
 */
@HiltAndroidApp
class NeuroFocusApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleUsageTracking()
    }

    /**
     * Enqueues the periodic usage tracking worker.
     * Uses KEEP policy to avoid replacing an existing worker.
     */
    private fun scheduleUsageTracking() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UsageTrackingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            UsageTrackingWorker.createPeriodicWorkRequest()
        )
    }
}
