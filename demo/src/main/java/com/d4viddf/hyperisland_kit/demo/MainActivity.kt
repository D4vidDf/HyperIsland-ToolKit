package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.d4viddf.hyperisland_kit.demo.ui.theme.HyperIslandToolKitTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check permission to determine start destination
        val hasNotificationPermission =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        val startDestination = if (hasNotificationPermission) {
            Navigation.Compatibility.route
        } else {
            Navigation.Welcome.route
        }

        setContent {
            HyperIslandToolKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    // --- UPDATED: Added NotificationLog to the list ---
                    val bottomBarScreens = listOf(
                        Navigation.Compatibility,
                        Navigation.Demos,
                        Navigation.NotificationLog
                    )

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            // Only show bottom bar if NOT on the Welcome screen
                            if (currentDestination?.route != Navigation.Welcome.route) {
                                NavigationBar {
                                    bottomBarScreens.forEach { screen ->
                                        NavigationBarItem(
                                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                                            label = { Text(screen.title) },
                                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                            onClick = {
                                                navController.navigate(screen.route) {
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
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Navigation.Welcome.route) {
                                WelcomeScreen(navController)
                            }
                            composable(Navigation.Compatibility.route) {
                                CompatibilityScreen(navController)
                            }
                            composable(Navigation.Demos.route) {
                                DemoListScreen(navController = navController)
                            }
                            // --- NEW ROUTE ---
                            composable(Navigation.NotificationLog.route) {
                                NotificationInspectorScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}