package com.neurofocus.app.presentation.block

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurofocus.app.presentation.theme.*

/**
 * BlockActivity – Full-screen "Daily Limit Reached" overlay.
 *
 * Launched by AppInterceptorService when a flagged app's daily usage
 * exceeds its configured time limit.
 *
 * BACK BUTTON BEHAVIOR:
 * Pressing Back routes the user to the Android Home Screen (Launcher),
 * NOT back to the blocked app. This prevents circumventing the block.
 */
class BlockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: ""
        val usageMinutes = intent.getIntExtra("usage_minutes", 0)
        val limitMinutes = intent.getIntExtra("limit_minutes", 30)
        val appName = resolveAppName(blockedPackage)

        setContent {
            NeuroFocusTheme {
                BlockScreen(
                    appName = appName,
                    usageMinutes = usageMinutes,
                    limitMinutes = limitMinutes,
                    onGoHome = { goToHomeScreen() }
                )
            }
        }
    }

    /**
     * Overrides back press to route to the Home Screen instead of
     * returning to the blocked app.
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goToHomeScreen()
    }

    /** Launches the default home launcher, clearing the task stack */
    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    /** Resolves a package name to a human-readable app label */
    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }
}

// ─── Block Screen UI ────────────────────────────────────────────

@Composable
private fun BlockScreen(
    appName: String,
    usageMinutes: Int,
    limitMinutes: Int,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0020), // Very dark purple
                        Color(0xFF0D0D0D)  // Near black
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ─── Block Icon ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(ScoreRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = "Blocked",
                modifier = Modifier.size(48.dp),
                tint = ScoreRed
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Title ──────────────────────────────────────────────
        Text(
            text = "Daily Limit Reached",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = appName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = NeuroLightPurple
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Usage Stats Card ───────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = ScoreOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Usage",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${usageMinutes} min used",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScoreRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daily limit: ${limitMinutes} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Usage bar
                LinearProgressIndicator(
                    progress = { (usageMinutes.toFloat() / limitMinutes).coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ScoreRed,
                    trackColor = ScoreRed.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Motivation Text ────────────────────────────────────
        Text(
            text = "You've used your daily screen time for this app.\nTake a break and focus on something productive! 💪",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Go Home Button ─────────────────────────────────────
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeuroMediumPurple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Go to Home Screen",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Pressing Back also takes you Home",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}
