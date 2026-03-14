package com.neurofocus.app.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.neurofocus.app.data.local.entity.UsageRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UsageStatsTracker – Integrates with Android's UsageStatsManager to collect
 * real-time app usage data.
 *
 * This class queries the system for usage events over a given time window, then
 * processes them to extract the four key signals used by the DopamineScoreEngine:
 *   1. Per-app time spent (foreground duration)
 *   2. App open count (foreground event count)
 *   3. Late-night usage (11 PM – 3 AM window)
 *   4. Rapid app switching (< 2 seconds between foreground transitions)
 *
 * IMPORTANT: Requires the PACKAGE_USAGE_STATS permission, which the user must
 * grant manually via Settings → Apps → Special access → Usage access.
 */
@Singleton
class UsageStatsTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Well-known social media package names for classification.
     * Used to flag apps contributing to the socialMediaOpenCount signal.
     */
    private val socialMediaPackages = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.twitter.android",
        "com.twitter.android.lite",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.reddit.frontpage",
        "com.pinterest",
        "com.tumblr",
        "com.linkedin.android",
        "com.whatsapp",
        "org.telegram.messenger",
        "com.discord",
        "com.google.android.youtube"
    )

    /**
     * Collects usage data for today and returns a list of UsageRecords.
     *
     * Processing steps:
     * 1. Query UsageEvents for the past 24 hours
     * 2. Track foreground/background transitions per app
     * 3. Calculate time spent, open count, late-night minutes
     * 4. Detect rapid app switching (< 2s between switches)
     */
    fun collectTodayUsage(): List<UsageRecord> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        // Start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val today = dateFormat.format(Date())

        return collectUsage(startTime, endTime, today)
    }

    /**
     * Core usage collection logic.
     *
     * Iterates through UsageEvents, tracks foreground start times, and
     * computes per-app metrics. Late-night detection checks if events
     * fall within the 23:00-03:00 window.
     */
    private fun collectUsage(startTime: Long, endTime: Long, date: String): List<UsageRecord> {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        // Track per-app data during processing
        data class AppData(
            var timeSpentMs: Long = 0,
            var openCount: Int = 0,
            var lateNightMinutes: Int = 0,
            var appSwitchCount: Int = 0,
            var lastForegroundTime: Long = 0,
            var isSocialMedia: Boolean = false
        )

        val appDataMap = mutableMapOf<String, AppData>()
        var lastForegroundPackage: String? = null
        var lastForegroundTimestamp: Long = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            val packageName = event.packageName ?: continue

            // Initialize app data if first encounter
            if (packageName !in appDataMap) {
                appDataMap[packageName] = AppData(
                    isSocialMedia = packageName in socialMediaPackages
                )
            }

            val appData = appDataMap[packageName]!!

            when (event.eventType) {
                // App moved to foreground
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    appData.openCount++
                    appData.lastForegroundTime = event.timeStamp

                    // Detect rapid app switching:
                    // If less than 2 seconds since last foreground event from a DIFFERENT app
                    if (lastForegroundPackage != null &&
                        lastForegroundPackage != packageName &&
                        (event.timeStamp - lastForegroundTimestamp) < 2000
                    ) {
                        appData.appSwitchCount++
                        // Also count for the previous app
                        appDataMap[lastForegroundPackage]?.let { it.appSwitchCount++ }
                    }

                    lastForegroundPackage = packageName
                    lastForegroundTimestamp = event.timeStamp
                }

                // App moved to background
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (appData.lastForegroundTime > 0) {
                        val duration = event.timeStamp - appData.lastForegroundTime
                        appData.timeSpentMs += duration

                        // Check if this usage period falls in late-night window (11PM-3AM)
                        if (isLateNightTime(appData.lastForegroundTime) ||
                            isLateNightTime(event.timeStamp)
                        ) {
                            appData.lateNightMinutes += (duration / 60000).toInt()
                        }

                        appData.lastForegroundTime = 0
                    }
                }
            }
        }

        // Convert to UsageRecord entities, filtering out very short usage
        return appDataMap
            .filter { it.value.timeSpentMs > 5000 } // Ignore < 5 seconds
            .map { (packageName, data) ->
                UsageRecord(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    timeSpentMs = data.timeSpentMs,
                    openCount = data.openCount,
                    lateNightMinutes = data.lateNightMinutes,
                    appSwitchCount = data.appSwitchCount,
                    date = date,
                    isSocialMedia = data.isSocialMedia
                )
            }
    }

    /**
     * Checks if a timestamp falls in the late-night window (11 PM – 3 AM).
     * This time range is associated with compulsive phone usage and
     * disrupted sleep patterns — a key behavioral indicator.
     */
    private fun isLateNightTime(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 23 || hour < 3
    }

    /**
     * Resolves a package name to a human-readable app name.
     * Falls back to the package name if resolution fails.
     */
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // Use last segment of package name as fallback
            packageName.substringAfterLast(".")
                .replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Checks if the app has the PACKAGE_USAGE_STATS permission.
     * This must be granted by the user in system settings.
     */
    fun hasUsageStatsPermission(): Boolean {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -1)
        val startTime = calendar.timeInMillis

        // If we can query stats without error, permission is granted
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        return stats != null && stats.isNotEmpty()
    }
}
