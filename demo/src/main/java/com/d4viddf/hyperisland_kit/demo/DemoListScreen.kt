package com.d4viddf.hyperisland_kit.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item { ConfigurableDemoCard { t, f, s -> DemoNotificationManager.showConfigurableNotification(context, t, f, s) } }

        // --- SECTION 1: OFFICIAL TEMPLATES (1-22) ---
        item { Text("Official Templates (1-22)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }

        item { DemoCard("1. Weather", "BaseInfo Type 1 (Red Alert)", onClick = { DemoNotificationManager.showTemplate1_Weather(context) }) }
        item {
            DemoCard(
                title = "2. Payment",
                description = "BaseInfo Type 2 (Right Icon). Replicates Xiaomi Bill Payment.",
                onClick = { DemoNotificationManager.showTemplate2_Payment(context) }
            )
        }
        item { DemoCard("3. IM / Chat", "ChatInfo (Messaging)", onClick = { DemoNotificationManager.showTemplate3_Chat(context) }) }
        item { DemoCard("4. Taxi Queue", "BaseInfo 2 + MultiProgress", onClick = { DemoNotificationManager.showTemplate4_TaxiQueue(context) }) }
        item { DemoCard("5. Dining Queue", "BaseInfo 1 + ProgressBar", onClick = { DemoNotificationManager.showTemplate5_DiningQueue(context) }) }
        item { DemoCard("6. Parking", "BaseInfo 2 + ProgressBar", onClick = { DemoNotificationManager.showTemplate6_Parking(context) }) }
        item { DemoCard("7. Uploading", "ChatInfo + Circular Progress", onClick = { DemoNotificationManager.showTemplate7_Upload(context) }) }
        item { DemoCard("8. Coupon", "ChatInfo + HintAction (Top Button)", onClick = { DemoNotificationManager.showTemplate8_Coupon(context) }) }
        item { DemoCard("9. Movie Ticket", "BaseInfo 2 + HintTimer (Top Timer)", onClick = { DemoNotificationManager.showTemplate9_Movie(context) }) }
        item { DemoCard("10. Pickup", "BaseInfo 2 + HintAction", onClick = { DemoNotificationManager.showTemplate10_Pickup(context) }) }
        item { DemoCard("11. Sports/Run", "HighlightInfo + HintTimer", onClick = { DemoNotificationManager.showTemplate11_Sports(context) }) }
        item { DemoCard("12. Call", "ChatInfo + Standard Actions", onClick = { DemoNotificationManager.showTemplate12_Call(context) }) }
        item { DemoCard("13. Recording", "HighlightInfo + Stop Action", onClick = { DemoNotificationManager.showTemplate13_Recording(context) }) }
        item { DemoCard("14. Navigation", "IconTextInfo (Turn Right)", onClick = { DemoNotificationManager.showTemplate14_Navigation(context) }) }
        item { DemoCard("15. Recorder", "AnimTextInfo (Voice Wave)", onClick = { DemoNotificationManager.showTemplate15_Recorder(context) }) }
        item { DemoCard("16. Code", "IconTextInfo + Copy Button", onClick = { DemoNotificationManager.showTemplate16_Code(context) }) }
        item { DemoCard("17. Promo", "HighlightInfoV3 (Pricing)", onClick = { DemoNotificationManager.showTemplate17_Promo(context) }) }
        item { DemoCard("18. File Request", "IconTextInfo + Text Buttons", onClick = { DemoNotificationManager.showTemplate18_FileRequest(context) }) }
        item { DemoCard("19. Cover Media", "CoverInfo + HintAction", onClick = { DemoNotificationManager.showTemplate19_Cover(context) }) }
        item { DemoCard("20. Data Usage", "IconTextInfo + Linear Progress", onClick = { DemoNotificationManager.showTemplate20_Data(context) }) }
        item { DemoCard("21. Game Download", "ChatInfo + Linear Progress", onClick = { DemoNotificationManager.showTemplate21_Game(context) }) }
        item { DemoCard("22. IoT Status", "IconTextInfo + Progress", onClick = { DemoNotificationManager.showTemplate22_IoT(context) }) }

        // --- SECTION 2: ADVANCED CUSTOMIZATION ---
        item { Text("Advanced Customization", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
        item { DemoCard("Colored Text Buttons", "Text-only actions with custom BG colors.", onClick = { DemoNotificationManager.showRawColoredTextButtons(context) }) }
        item { DemoCard("Icon Buttons (Rounded)", "Icon-only actions with generated circle BG.", onClick = { DemoNotificationManager.showRawIconButtons(context) }) }
        item { DemoCard("Mix: Progress + Color", "Circular progress button next to a standard colored button.", onClick = { DemoNotificationManager.showRawProgressAndColorButton(context) }) }
        item { DemoCard("Background Color", "Notification with custom colored background.", onClick = { DemoNotificationManager.showRawBgInfo(context) }) }

        // --- SECTION 3: STANDARD DEMOS ---
        item { Text("Standard Demos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp)) }
        item { DemoCard("App Open Demo", "Basic notification. Drag island to open.", onClick = { DemoNotificationManager.showAppOpenNotification(context) }) }
        item { DemoCard("Chat Info", "Chat style with text action.", onClick = { DemoNotificationManager.showChatNotification(context) }) }
        item { DemoCard("Simple Island", "Small icon left, text right.", onClick = { DemoNotificationManager.showSimpleSmallIslandNotification(context) }) }
        item { DemoCard("Right Image", "Expanded island with image on right.", onClick = { DemoNotificationManager.showRightImageNotification(context) }) }
        item { DemoCard("Split Info", "Content on both left and right sides.", onClick = { DemoNotificationManager.showSplitIslandNotification(context) }) }
        item { DemoCard("Hint Info", "Small hint floating above notification.", onClick = { DemoNotificationManager.showHintInfoNotification(context) }) }
        item { DemoCard("Multi Node Progress", "Segmented 'Step 2 of 4' progress bar.", onClick = { DemoNotificationManager.showMultiNodeProgressNotification(context) }) }
        item { DemoCard("Icon Progress Bar", "Linear progress with icons (Delivery).", onClick = { DemoNotificationManager.showProgressBarNotification(context) }) }
        item { DemoCard("Circular Progress", "Circular progress on big/small island.", onClick = { DemoNotificationManager.showCircularProgressNotification(context) }) }
        item { DemoCard("Countdown Timer", "15-minute countdown.", onClick = { DemoNotificationManager.showCountdownNotification(context) }) }
        item { DemoCard("Count-Up Timer", "Timer counting up.", onClick = { DemoNotificationManager.showCountUpNotification(context) }) }
        item { DemoCard("Multi-Action", "Stop (Progress) + Close buttons.", onClick = { DemoNotificationManager.showMultiActionNotification(context) }) }

        item {
            DemoCard(
                title = "Focus DIY (Custom View)",
                description = "Uses 'miui.focus.rv' and 'param.custom' to render a pure RemoteView island.",
                onClick = { DemoNotificationManager.showFocusDiyNotification(context) }
            )
        }

        item {
            DemoCard(
                title = "Music Player (DIY)",
                description = "Custom RemoteView with Timeline, Controls, and Dynamic Color.",
                onClick = { DemoNotificationManager.showMusicPlayerDemo(context) }
            )
        }
    }


}

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