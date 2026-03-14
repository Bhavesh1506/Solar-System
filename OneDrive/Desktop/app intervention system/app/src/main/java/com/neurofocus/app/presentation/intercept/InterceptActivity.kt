package com.neurofocus.app.presentation.intercept

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.neurofocus.app.service.AppInterceptorService
import dagger.hilt.android.AndroidEntryPoint

/**
 * InterceptActivity – The "Pay 50 XP to Open" screen.
 *
 * Launched by AppInterceptorService when a blocked app is detected.
 * On successful payment:
 *   1. Deducts 50 XP from the local database
 *   2. Sets AppInterceptorService.bypassedPackage = the blocked package
 *   3. Finishes itself, returning the user to the (now-bypassed) app
 *
 * The bypass remains active as long as the user stays in that specific app.
 * Switching to any other app or the home screen clears the bypass.
 */
@AndroidEntryPoint
class InterceptActivity : ComponentActivity() {

    private val viewModel: InterceptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra("blocked_package") ?: ""
        val appName = resolveAppName(blockedPackage)

        viewModel.loadBalance()

        setContent {
            NeuroFocusTheme {
                InterceptScreen(
                    appName = appName,
                    packageName = blockedPackage,
                    viewModel = viewModel,
                    onDismiss = { finish() },
                    onPaymentSuccess = {
                        // Set the session bypass flag so the service allows this app
                        AppInterceptorService.bypassedPackage = blockedPackage
                        finish()
                    }
                )
            }
        }
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }
}

// ─── Intercept Screen UI ────────────────────────────────────────

@Composable
private fun InterceptScreen(
    appName: String,
    packageName: String,
    viewModel: InterceptViewModel,
    onDismiss: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Auto-close on successful payment
    LaunchedEffect(state.result) {
        if (state.result == InterceptViewModel.PayResult.SUCCESS) {
            onPaymentSuccess()
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
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(ScoreRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(44.dp),
                tint = ScoreRed
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Title ──────────────────────────────────────────────
        Text(
            text = "App Blocked",
            fontSize = 30.sp,
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

        // ─── XP Balance ─────────────────────────────────────────
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

        // ─── Pay Button ─────────────────────────────────────────
        val hasEnoughXp = state.xpBalance >= state.xpCost

        Button(
            onClick = { viewModel.payXp() },
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
                    text = "Pay ${state.xpCost} XP to Open",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // ─── Warning if insufficient XP ─────────────────────────
        if (!hasEnoughXp || state.result == InterceptViewModel.PayResult.INSUFFICIENT) {
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

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Go Back ────────────────────────────────────────────
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
