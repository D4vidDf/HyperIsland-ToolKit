package com.d4viddf.hyperisland_kit.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues // Import PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DemoListScreen(
    // --- REMOVED MODIFIER PARAMETER ---
    navController: NavController
) {
    val context = LocalContext.current

    // Check if permission was revoked while app was in background
    CheckPermissionLost(navController = navController)

    LazyColumn(
        // --- MODIFIED MODIFIER ---
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        // --- ADDED CONTENT PADDING ---
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Card 1: App Open Demo
        item {
            DemoCard(
                title = "App Open Demo",
                description = "A basic notification. Tap the notification or drag the island to open the app.",
                onClick = {
                    DemoNotificationManager.showAppOpenNotification(context)
                }
            )
        }

        // Card 2: Chat Notification Demo
        item {
            DemoCard(
                title = "Chat Notification",
                description = "Shows a 'chat' style (expanded) and simple (summary) view with a progress button.",
                onClick = {
                    DemoNotificationManager.showChatNotification(context)
                }
            )
        }

        // Card 3: Countdown Notification Demo
        item {
            DemoCard(
                title = "Countdown Notification",
                description = "Shows a 15-minute countdown in chat, big island (expanded), and summary views.",
                onClick = {
                    DemoNotificationManager.showCountdownNotification(context)
                }
            )
        }

        // Card 4: Linear Progress Bar
        item {
            DemoCard(
                title = "Linear Progress Bar",
                description = "Shows a 'chat' style (expanded) with a linear progress bar below it.",
                onClick = {
                    DemoNotificationManager.showProgressBarNotification(context)
                }
            )
        }

        // Card 5: Circular Progress Demo
        item {
            DemoCard(
                title = "Circular Progress Demo",
                description = "Shows circular progress on both the big island (expanded) and small island (summary).",
                onClick = {
                    DemoNotificationManager.showCircularProgressNotification(context)
                }
            )
        }

        // Card 6: Count-Up Timer
        item {
            DemoCard(
                title = "Count-Up Timer",
                description = "Shows a timer counting *up* in the expanded view and a simple icon in summary.",
                onClick = {
                    DemoNotificationManager.showCountUpNotification(context)
                }
            )
        }

        // Card 7: Simple Small Island
        item {
            DemoCard(
                title = "Simple Small Island",
                description = "Uses 'baseInfo' and shows a simple icon (summary) and icon+text (expanded).",
                onClick = {
                    DemoNotificationManager.showSimpleSmallIslandNotification(context)
                }
            )
        }
    }
}

// ... (DemoCard composable is the same) ...
@Composable
fun DemoCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}