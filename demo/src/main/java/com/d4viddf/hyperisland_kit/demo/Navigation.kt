package com.d4viddf.hyperisland_kit.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed class to define our navigation destinations
sealed class Navigation(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Welcome : Navigation(
        route = "welcome",
        title = "welcome",
        icon = Icons.Default.Notifications
    )
    object Compatibility : Navigation(
        route = "compatibility",
        title = "Check",
        icon = Icons.Default.CheckCircle
    )
    object Demos : Navigation(
        route = "demos",
        title = "Demos",
        icon = Icons.AutoMirrored.Filled.List
    )
}