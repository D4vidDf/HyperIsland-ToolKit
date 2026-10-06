package com.d4viddf.hyperisland_kit.demo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.d4viddf.hyperisland_kit.demo.ui.screens.compatibility.CompatibilityScreen
import com.d4viddf.hyperisland_kit.demo.ui.screens.compatibility.DeviceDiagnosticsScreen
import com.d4viddf.hyperisland_kit.demo.ui.screens.demos.DemoListScreen
import com.d4viddf.hyperisland_kit.demo.ui.screens.inspector.NotificationDetailScreen
import com.d4viddf.hyperisland_kit.demo.ui.screens.inspector.NotificationInspectorScreen
import com.d4viddf.hyperisland_kit.demo.ui.screens.inspector.NotificationInspectorViewModel
import com.d4viddf.hyperisland_kit.demo.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavigation(
    startDestination: String,
    inspectorViewModel: NotificationInspectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    val bottomBarScreens = listOf(
        NavigationDestination.Compatibility,
        NavigationDestination.Demos,
        NavigationDestination.NotificationLog
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isWelcomeScreen = currentRoute == NavigationDestination.Welcome.route
    val isDetailOrSubScreen = currentRoute?.startsWith("notification_detail") == true || currentRoute == "device_diagnostics"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isExpanded && !isWelcomeScreen && !isDetailOrSubScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    bottomBarScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isExpanded && !isWelcomeScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    bottomBarScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationRailItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavigationDestination.Welcome.route) {
                        WelcomeScreen(navController = navController)
                    }
                    composable(NavigationDestination.Compatibility.route) {
                        CompatibilityScreen(navController = navController)
                    }
                    composable("device_diagnostics") {
                        DeviceDiagnosticsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(NavigationDestination.Demos.route) {
                        DemoListScreen(navController = navController)
                    }
                    composable(NavigationDestination.NotificationLog.route) {
                        NotificationInspectorScreen(
                            onSelectNotification = { key ->
                                navController.navigate("notification_detail/$key")
                            },
                            viewModel = inspectorViewModel
                        )
                    }
                    composable(
                        route = "notification_detail/{notificationKey}",
                        arguments = listOf(navArgument("notificationKey") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val key = backStackEntry.arguments?.getString("notificationKey") ?: ""
                        NotificationDetailScreen(
                            notificationKey = key,
                            viewModel = inspectorViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
