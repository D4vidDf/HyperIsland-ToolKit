package com.d4viddf.hyperisland_kit.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DemoListScreen(
    navController: NavController
) {
    val context = LocalContext.current

    CheckPermissionLost(navController = navController)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // --- 0. Configurable Options ---
        item {
            ConfigurableDemoCard(
                onClick = { timeout, enableFloat, showInShade ->
                    DemoNotificationManager.showConfigurableNotification(
                        context, timeout, enableFloat, showInShade
                    )
                }
            )
        }

        // --- 1. Simple Demos ---
        item { DemoCard("App Open Demo", "Basic notification. Drag island to open.") { DemoNotificationManager.showAppOpenNotification(context) } }
        item { DemoCard("Chat Info (Text Action)", "Chat style with text-only action.", onClick = { DemoNotificationManager.showChatNotification(context) }) }
        item { DemoCard("Chat Info (Icon Action)", "Simple panel with icon button.", onClick = { DemoNotificationManager.showSimpleSmallIslandNotification(context) }) }
        item { DemoCard("Right Image Demo", "Expanded island with image on right.", onClick = { DemoNotificationManager.showRightImageNotification(context) }) }
        item { DemoCard("Split Info (Left & Right)", "Content on both left and right sides.", onClick = { DemoNotificationManager.showSplitIslandNotification(context) }) }

        // --- NEW DEMOS ---
        item { DemoCard("Hint Info (Top)", "Small hint floating above notification.", onClick = { DemoNotificationManager.showHintInfoNotification(context) }) }
        item { DemoCard("Node Progress (Multi)", "Segmented 'Step 2 of 4' progress bar on notification.", onClick = { DemoNotificationManager.showMultiNodeProgressNotification(context) }) }
        item { DemoCard("Colored Title (BaseInfo)", "Standard notification with Red title.", onClick = { DemoNotificationManager.showColoredBaseNotification(context) }) }

        // --- 2. Progress & Timers ---
        item { DemoCard("Icon Progress Bar", "Linear progress with icons.", onClick = { DemoNotificationManager.showProgressBarNotification(context) }) }
        item { DemoCard("Circular Progress Demo", "Circular progress on big/small island.", onClick = { DemoNotificationManager.showCircularProgressNotification(context) }) }
        item { DemoCard("Countdown Notification", "15-minute countdown timer.", onClick = { DemoNotificationManager.showCountdownNotification(context) }) }
        item { DemoCard("Count-Up Timer", "Timer counting up.", onClick = { DemoNotificationManager.showCountUpNotification(context) }) }

        // --- 3. Multi-Action ---
        item { DemoCard("Multi-Action Demo", "Progress button + Text button.", onClick = { DemoNotificationManager.showMultiActionNotification(context) }) }
    }
}

// ... (DemoCard and ConfigurableDemoCard remain unchanged) ...
@Composable
fun DemoCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun ConfigurableDemoCard(onClick: (timeout: Long, enableFloat: Boolean, showInShade: Boolean) -> Unit) {
    var timeoutSeconds by remember { mutableFloatStateOf(5f) }
    var enableFloat by remember { mutableStateOf(true) }
    var showInShade by remember { mutableStateOf(true) }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Configurable Options Demo", style = MaterialTheme.typography.titleLarge)
            Text(text = "Test timeout, float, and visibility settings.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
            Text(text = "Timeout: ${timeoutSeconds.toInt()}s")
            Slider(value = timeoutSeconds, onValueChange = { timeoutSeconds = it }, valueRange = 0f..60f, steps = 59)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Enable Float", modifier = Modifier.weight(1f))
                Switch(checked = enableFloat, onCheckedChange = { enableFloat = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Show in Shade", modifier = Modifier.weight(1f))
                Switch(checked = showInShade, onCheckedChange = { showInShade = it })
            }
            Button(onClick = { onClick((timeoutSeconds * 1000).toLong(), enableFloat, showInShade) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Show Configured Notification")
            }
        }
    }
}