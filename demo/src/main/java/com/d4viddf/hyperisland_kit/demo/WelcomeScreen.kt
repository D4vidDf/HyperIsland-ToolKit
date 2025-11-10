package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController

@Composable
fun WelcomeScreen(navController: NavController) {
    val context = LocalContext.current
    var hasBeenDenied by remember { mutableStateOf(false) }

    // This effect runs when the screen is first composed AND
    // when the user returns to the app (e.g., from settings)
    CheckPermissionOnResume(
        onPermissionGranted = {
            // Permission is already granted, so go to the main app
            navController.navigate(Navigation.Compatibility.route) {
                popUpTo(Navigation.Welcome.route)
            }
        }
    )

    // Create a permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // GRANTED! Navigate to the main app.
                navController.navigate(Navigation.Compatibility.route) {
                    popUpTo(Navigation.Welcome.route)
                }
            } else {
                // DENIED. Stay on this screen.
                hasBeenDenied = true // Mark that user has denied it at least once
                Toast.makeText(context, "Permission is required to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Navigation.Welcome.icon,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Welcome to the Demo",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "This app demonstrates HyperIsland notifications. To show notifications on this device, we need your permission.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        if (hasBeenDenied) {
            Text(
                text = "You have denied the permission. Please grant it from your phone's settings to continue.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = {
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            }) {
                Text("Open Settings")
            }
        } else {
            Button(
                onClick = {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                ) {
                Text("Grant Permission")
            }
        }
    }
}

/**
 * A helper composable that checks for permission ON_RESUME.
 * If permission is granted, it navigates away from the Welcome screen.
 */
@Composable
private fun CheckPermissionOnResume(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    onPermissionGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}