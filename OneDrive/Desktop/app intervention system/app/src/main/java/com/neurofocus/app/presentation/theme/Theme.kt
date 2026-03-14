package com.neurofocus.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeuroLightPurple,
    onPrimary = NeuroDeepPurple,
    primaryContainer = NeuroMediumPurple,
    onPrimaryContainer = NeuroLightPurple,
    secondary = NeuroLightTeal,
    onSecondary = NeuroTeal,
    secondaryContainer = NeuroTeal,
    onSecondaryContainer = NeuroLightTeal,
    tertiary = XpGold,
    background = DarkSurface,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Purple80,
    onSurface = Purple80,
    onSurfaceVariant = PurpleGrey80
)

private val LightColorScheme = lightColorScheme(
    primary = NeuroMediumPurple,
    onPrimary = LightSurface,
    primaryContainer = NeuroLightPurple,
    onPrimaryContainer = NeuroDeepPurple,
    secondary = NeuroTeal,
    onSecondary = LightSurface,
    secondaryContainer = NeuroLightTeal,
    onSecondaryContainer = NeuroTeal,
    tertiary = XpGold,
    background = LightSurface,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = NeuroDeepPurple,
    onSurface = NeuroDeepPurple,
    onSurfaceVariant = PurpleGrey40
)

private val NeuroFocusTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleLarge = Typography().titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = Typography().titleMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = Typography().bodyLarge.copy(fontSize = 16.sp),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 14.sp),
    labelLarge = Typography().labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    labelMedium = Typography().labelMedium.copy(fontSize = 12.sp)
)

@Composable
fun NeuroFocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NeuroFocusTypography,
        content = content
    )
}
