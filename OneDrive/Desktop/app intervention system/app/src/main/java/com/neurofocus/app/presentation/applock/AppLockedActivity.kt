package com.neurofocus.app.presentation.applock

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neurofocus.app.presentation.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * AppLockedActivity – Full-screen overlay shown when a blocked app is opened.
 *
 * Displays:
 * - The blocked app's name and icon
 * - "App Locked" message with current XP balance
 * - "Pay 50 XP to unlock for 15 minutes" button
 * - Insufficient XP warning if balance is too low
 *
 * Launched by AppInterceptorService when a flagged app enters the foreground.
 */
@AndroidEntryPoint
class AppLockedActivity : ComponentActivity() {

    private val viewModel: AppLockedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: ""
        val appName = resolveAppName(blockedPackage)

        viewModel.loadXpBalance()

        setContent {
            NeuroFocusTheme {
                AppLockedScreen(
                    appName = appName,
                    packageName = blockedPackage,
                    viewModel = viewModel,
                    onDismiss = { finish() },
                    onUnlockSuccess = {
                        // Close the lock screen so the blocked app can resume
                        finish()
                    }
                )
            }
        }
    }

    /** Resolves a package name to a human-readable app name */
    private fun resolveAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }
}

// ─── Lock Screen Composable ─────────────────────────────────────

@Composable
private fun AppLockedScreen(
    appName: String,
    packageName: String,
    viewModel: AppLockedViewModel,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Auto-dismiss on successful unlock
    LaunchedEffect(state.unlockResult) {
        if (state.unlockResult == AppLockedViewModel.UnlockResult.SUCCESS) {
            onUnlockSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeuroDeepPurple,
                        DarkSurface
                    )
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ─── Lock Icon ──────────────────────────────────────────
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size((80 * pulseScale).dp)
                .clip(CircleShape)
                .background(ScoreRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(40.dp),
                tint = ScoreRed
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── App Name ───────────────────────────────────────────
        Text(
            text = "App Locked",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = appName,
            fontSize = 20.sp,
            color = NeuroLightPurple,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── XP Balance Card ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your XP Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✨ ${state.xpBalance} XP",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = XpGold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Pay to Unlock Button ───────────────────────────────
        val hasEnoughXp = state.xpBalance >= state.xpCost

        Button(
            onClick = { viewModel.payToUnlock(packageName) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = hasEnoughXp && !state.isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasEnoughXp) NeuroTeal else Color.Gray,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay ${state.xpCost} XP · Unlock ${state.unlockMinutes} min",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // ─── Insufficient XP Warning ────────────────────────────
        if (!hasEnoughXp) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ScoreRed.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ScoreOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Not enough XP! Complete focus sessions to earn more.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScoreOrange
                    )
                }
            }
        }

        if (state.unlockResult == AppLockedViewModel.UnlockResult.INSUFFICIENT) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ Transaction failed – insufficient XP",
                color = ScoreRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Go Back Button ─────────────────────────────────────
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Go Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Earn XP by completing focus sessions in NeuroFocus",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
