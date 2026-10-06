package com.d4viddf.hyperisland_kit.demo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Welcome : NavigationDestination(
        route = "welcome",
        title = "Welcome",
        icon = Icons.Default.Notifications
    )

    object Compatibility : NavigationDestination(
        route = "compatibility",
        title = "Check",
        icon = Icons.Default.CheckCircle
    )

    object Demos : NavigationDestination(
        route = "demos",
        title = "Demos",
        icon = Icons.AutoMirrored.Filled.List
    )

    object NotificationLog : NavigationDestination(
        route = "notification_log",
        title = "Inspector",
        icon = Icons.Default.Search
    )
}
