package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.concurrent.TimeUnit

// ... (keys are the same) ...
private const val ACTION_KEY_TAKEN = "action.taken"
private const val PIC_KEY_MEDICATION = "pic.medication"
private const val PIC_KEY_DEMO_ICON = "pic.demo.icon"
private const val PIC_KEY_PROGRESS = "pic.progress"
private const val PIC_KEY_COUNTUP = "pic.countup"
private const val PIC_KEY_SIMPLE = "pic.simple"
private const val PIC_KEY_APP_OPEN = "pic.app.open"

object DemoNotificationManager {

    // ... (hasNotificationPermission, getUniqueNotificationId, showSupportToast, createSimpleAZone are the same) ...
    private fun hasNotificationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun getUniqueNotificationId() = System.currentTimeMillis().toInt()

    private fun showSupportToast(context: Context) {
        if (!HyperIslandNotification.isSupported(context)) {
            Toast.makeText(context, "HyperIsland not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }
    private fun createSimpleAZone(picKey: String, text: String): ImageTextInfoLeft {
        return ImageTextInfoLeft(
            picInfo = PicInfo(type = 1, pic = picKey),
            textInfo = TextInfo(title = text)
        )
    }

    // ... (showChatNotification, showCountdownNotification, showProgressBarNotification, showCountUpNotification, showSimpleSmallIslandNotification, showAppOpenNotification are the same as previous step) ...
    // ... (I've ommitted them for brevity, but they are unchanged from the previous step) ...

    fun showChatNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Chat Notification"
        val text = "This demonstrates the 'ChatInfo' template."
        val dummyIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val takenAction = HyperAction(
            key = ACTION_KEY_TAKEN,
            title = "Mark as Taken",
            icon = Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            pendingIntent = dummyIntent,
            isProgressButton = true,
            progress = 25,
            progressColor = "#FF8514"
        )
        val medPicture = HyperPicture(PIC_KEY_MEDICATION, context, R.drawable.ic_launcher_foreground)

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setChatInfo( // Expanded View
                title = "Ibuprofen",
                content = "Next dose: 30 minutes",
                pictureKey = PIC_KEY_MEDICATION,
                actionKey = ACTION_KEY_TAKEN
            )
            .setBigIslandInfo( // Also need a BigIslandArea
                createSimpleAZone(PIC_KEY_MEDICATION, "Ibuprofen")
            )
            .setSmallIsland( // Summary View
                aZone = createSimpleAZone(PIC_KEY_MEDICATION, "Chat"),
                bZone = null
            )
            .addAction(takenAction)
            .addPicture(medPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(dummyIntent) // Add this for app open
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Countdown Notification"
        val text = "This demonstrates a countdown timer."
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)

        val countdownTimer = TimerInfo(
            timerType = -1, // -1 for countdown
            timerWhen = countdownTime,
            timerTotal = System.currentTimeMillis(),
            timerSystemCurrent = System.currentTimeMillis()
        )
        val demoPicture = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo( // Expanded View
                title = "Pizza in oven",
                timer = countdownTimer,
                pictureKey = PIC_KEY_DEMO_ICON
            )
            .setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON) // Expanded View
            .setSmallIsland( // Summary View
                aZone = createSimpleAZone(PIC_KEY_DEMO_ICON, "15:00"),
                bZone = null
            )
            .addPicture(demoPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Progress Bar Demo"
        val text = "Showing linear progress bar"
        val progress = 60 // 60%
        val progressColor = "#007AFF" // Blue

        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.ic_launcher_foreground)

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo( // Expanded View
                title = "Uploading file...",
                content = "60% complete",
                pictureKey = PIC_KEY_PROGRESS
            )
            .setProgressBar(progress, progressColor) // Expanded View
            .setBigIslandInfo( // Also need a BigIslandArea
                createSimpleAZone(PIC_KEY_PROGRESS, "Uploading...")
            )
            .setSmallIsland( // Summary View
                aZone = createSimpleAZone(PIC_KEY_PROGRESS, "60%"),
                bZone = null
            )
            .addPicture(progressPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    // --- MODIFIED THIS FUNCTION ---
    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Circular Progress Demo"
        val text = "Showing circular progress on island"
        val progress = 75 // 75%
        val progressColor = "#34C759" // Green

        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.ic_launcher_foreground)

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo( // Expanded Panel
                title = "Downloading...",
                content = "75% complete",
                pictureKey = PIC_KEY_PROGRESS
            )
            // --- MODIFIED: Use setBigIslandProgressCircle ---
            .setBigIslandProgressCircle( // Expanded Island
                pictureKey = PIC_KEY_PROGRESS,
                title = "Downloading", // <-- Text is on the left
                progress = progress,
                color = progressColor,
                isCCW = true
            )
            // --- MODIFIED: Use setSmallIslandCircularProgress ---
            .setSmallIslandCircularProgress( // Summary Island
                pictureKey = PIC_KEY_PROGRESS,
                progress = progress,
                color = progressColor,
                isCCW = true
            )
            .addPicture(progressPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Count-Up Timer"
        val text = "This demonstrates a count-up timer."
        val startTime = System.currentTimeMillis()

        val countUpTimer = TimerInfo(
            timerType = 1, // 1 for count-up
            timerWhen = startTime,
            timerTotal = startTime,
            timerSystemCurrent = System.currentTimeMillis()
        )
        val countUpPicture = HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.ic_launcher_foreground)

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setChatInfo( // Expanded Panel (Rule A)
                title = "Recording...",
                timer = countUpTimer,
                pictureKey = PIC_KEY_COUNTUP
            )
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP) // Expanded Island (Rule B)
            .setSmallIslandIcon(PIC_KEY_COUNTUP) // Summary Island (Rule B)
            .addPicture(countUpPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "Simple Small Island"
        val text = "Icon on left (small), icon+text (big)."

        val simplePicture = HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.ic_launcher_foreground)

        // Define the Big Island content (Template 1)
        val bigIslandInfo = ImageTextInfoLeft(
            type = 1,
            picInfo = PicInfo(type = 1, pic = PIC_KEY_SIMPLE),
            textInfo = TextInfo(title = "Simple Info", content = "This is the expanded view")
        )

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setBaseInfo( // Expanded Panel (Rule A)
                title = "Simple Info",
                content = "This is the expanded view",
                pictureKey = PIC_KEY_SIMPLE
            )
            .setBigIslandInfo(bigIslandInfo) // Expanded Island (Rule B)
            .setSmallIslandIcon(PIC_KEY_SIMPLE) // Summary Island (Rule B)
            .addPicture(simplePicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val title = "App Open Demo"
        val text = "Tap or drag to open the app."

        val appOpenPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.ic_launcher_foreground)

        // This is the intent for a standard notification tap
        val dummyIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Define the Big Island content
        val bigIslandInfo = ImageTextInfoLeft(
            type = 1,
            picInfo = PicInfo(type = 1, pic = PIC_KEY_APP_OPEN),
            textInfo = TextInfo(title = "Open Demo", content = "Tap or drag")
        )

        val hyperIslandExtras = HyperIslandNotification
            .Builder(context, "demoApp", title)
            // --- This enables the "drag to open" ---
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setBaseInfo( // Expanded Panel
                title = "App Open Demo",
                content = "Tap or drag to open the app",
                pictureKey = PIC_KEY_APP_OPEN
            )
            .setBigIslandInfo(bigIslandInfo) // Expanded Island
            .setSmallIslandIcon(PIC_KEY_APP_OPEN) // Summary Island
            .addPicture(appOpenPicture)
            .buildExtras()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            // --- This enables the standard "tap to open" ---
            .setContentIntent(dummyIntent)
            .addExtras(hyperIslandExtras)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }
}