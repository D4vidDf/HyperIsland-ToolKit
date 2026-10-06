package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.d4viddf.hyperisland_kit.demo.ui.navigation.AppNavigation
import com.d4viddf.hyperisland_kit.demo.ui.navigation.NavigationDestination
import com.d4viddf.hyperisland_kit.demo.ui.theme.HyperIslandToolKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        val startDestination = if (hasNotificationPermission) {
            NavigationDestination.Compatibility.route
        } else {
            NavigationDestination.Welcome.route
        }

        setContent {
            HyperIslandToolKitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}
