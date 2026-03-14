package com.neurofocus.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.neurofocus.app.data.usage.UsageStatsTracker
import com.neurofocus.app.presentation.navigation.NeuroFocusNavGraph
import com.neurofocus.app.presentation.theme.NeuroFocusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity – Entry point of the NeuroFocus app.
 *
 * Uses @AndroidEntryPoint for Hilt injection. On first launch, prompts
 * the user to grant Usage Stats permission (required for tracking).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var usageStatsTracker: UsageStatsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prompt for Usage Stats permission if not granted
        if (!usageStatsTracker.hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        setContent {
            NeuroFocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NeuroFocusNavGraph()
                }
            }
        }
    }
}
