package com.neurofocus.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.neurofocus.app.presentation.analytics.AnalyticsScreen
import com.neurofocus.app.presentation.dashboard.DashboardScreen
import com.neurofocus.app.presentation.goals.FocusTimerScreen
import com.neurofocus.app.presentation.goals.GoalsScreen
import com.neurofocus.app.presentation.intervention.InterventionScreen
import com.neurofocus.app.presentation.settings.SettingsScreen

/**
 * Route definitions for the navigation graph.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Analytics : Screen("analytics")
    object Goals : Screen("goals")
    object Settings : Screen("settings")
    object FocusTimer : Screen("focus_timer/{goalId}") {
        fun createRoute(goalId: Long) = "focus_timer/$goalId"
    }
    object Intervention : Screen("intervention")
}

/**
 * Bottom navigation tab configuration.
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(Screen.Analytics, "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    BottomNavItem(Screen.Goals, "Goals", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

/**
 * Main scaffold with bottom navigation and NavHost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuroFocusNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on focus timer and intervention screens
    val showBottomBar = currentDestination?.route?.let { route ->
        route in bottomNavItems.map { it.screen.route }
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToIntervention = {
                        navController.navigate(Screen.Intervention.route)
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Goals.route) {
                GoalsScreen(
                    onStartFocusSession = { goalId ->
                        navController.navigate(Screen.FocusTimer.createRoute(goalId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.FocusTimer.route,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                FocusTimerScreen(
                    goalId = goalId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Intervention.route) {
                InterventionScreen(
                    onDismiss = { navController.popBackStack() },
                    onStartFocusSession = { goalId ->
                        navController.popBackStack()
                        navController.navigate(Screen.FocusTimer.createRoute(goalId))
                    }
                )
            }
        }
    }
}
