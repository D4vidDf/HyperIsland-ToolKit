package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoRight
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.concurrent.TimeUnit

// --- Keys ---
private const val ACTION_KEY_TAKEN = "action.taken"
private const val ACTION_KEY_APP_OPEN = "action.app.open"
private const val ACTION_KEY_STOP_PROGRESS = "action.stop"
private const val ACTION_KEY_CLOSE_NOTIFICATION = "action.close"
private const val PIC_KEY_MEDICATION = "pic.medication"
private const val PIC_KEY_DEMO_ICON = "pic.demo.icon"
private const val PIC_KEY_PROGRESS = "pic.progress"
private const val PIC_KEY_COUNTUP = "pic.countup"
private const val PIC_KEY_SIMPLE = "pic.simple"
private const val PIC_KEY_APP_OPEN = "pic.app.open"
private const val PIC_KEY_STOP_ICON = "pic.stop"
private const val PIC_KEY_CLOSE_ICON = "pic.close"
private const val PIC_KEY_RIGHT_SIDE = "pic.right.side"

private const val TAG = "DemoNotifManager"

object DemoNotificationManager {

    // ... (All helper methods: hasNotificationPermission, getUniqueNotificationId, etc. remain unchanged) ...
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
    private fun createAppOpenIntent(context: Context, requestCode: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createBroadcastIntent(
        context: Context,
        notificationId: Int,
        action: String,
        toastMessage: String?,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            if (toastMessage != null) {
                putExtra(NotificationActionReceiver.EXTRA_TOAST_MESSAGE, toastMessage)
            }
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    // --- NEW: Configurable Notification Demo ---
    fun showConfigurableNotification(
        context: Context,
        timeout: Long,
        enableFloat: Boolean,
        isShownNotification: Boolean
    ) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)

        val notificationId = getUniqueNotificationId()
        val title = "Configurable Demo"
        val text = "Timeout: ${timeout}ms, Float: $enableFloat"

        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)

        val hyperIslandBuilder = HyperIslandNotification
            .Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setChatInfo(
                title = title,
                content = text,
                pictureKey = PIC_KEY_DEMO_ICON
            )
            .setBigIslandInfo(
                createSimpleAZone(PIC_KEY_DEMO_ICON, "Configured")
            )
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .addPicture(demoPicture)
            // --- APPLY NEW CONFIGURATIONS ---
            .setEnableFloat(enableFloat)
            .setShowNotification(isShownNotification)

        // Only set timeout if it's greater than 0
        if (timeout > 0) {
            hyperIslandBuilder.setTimeout(timeout)
        }

        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppIntent)
            .addExtras(resourceBundle)
            .build()

        notification.extras.putString("miui.focus.param", jsonParam)

        Log.d(TAG, "showConfigurableNotification: Posting notification")
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    // ... (showChatNotification, showCountdownNotification, showProgressBarNotification, showCircularProgressNotification, showCountUpNotification, showSimpleSmallIslandNotification, showAppOpenNotification, showMultiActionNotification, showSplitIslandNotification, showRightImageNotification are all unchanged from previous version) ...

    // PLEASE INCLUDE THE EXISTING METHODS HERE WHEN COPYING.
    // I am truncating them for brevity, but the file structure should be maintained.

    fun showChatNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val notificationId = getUniqueNotificationId()
        val title = "Chat Notification"
        val text = "This demonstrates the 'ChatInfo' template."
        val openAppIntent = createAppOpenIntent(context, 0)
        val takenPendingIntent = createAppOpenIntent(context, 1)
        val takenAction = HyperAction(key = ACTION_KEY_TAKEN, title = "Open App", pendingIntent = takenPendingIntent, actionIntentType = 1, actionBgColor = "#007AFF")
        val medPicture = HyperPicture(PIC_KEY_MEDICATION, context, R.drawable.rounded_medication_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setChatInfo(title = "Ibuprofen", content = "Next dose: 30 minutes", pictureKey = PIC_KEY_MEDICATION, actionKeys = listOf(ACTION_KEY_TAKEN))
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_MEDICATION, "Ibuprofen"))
            .setSmallIslandIcon(PIC_KEY_MEDICATION)
            .addAction(takenAction)
            .addPicture(medPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Countdown Notification"
        val text = "This demonstrates a countdown timer."
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val countdownTimer = TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val demoPicture = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Pizza in oven", timer = countdownTimer, pictureKey = PIC_KEY_DEMO_ICON)
            .setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON)
            .setSmallIslandIcon(PIC_KEY_DEMO_ICON)
            .addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.rounded_timer_arrow_down_24).setContentTitle(title).setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Progress Bar Demo"
        val text = "Showing linear progress bar"
        val progress = 60
        val progressColor = "#007AFF"
        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_upload_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Uploading file...", content = "60% complete", pictureKey = PIC_KEY_PROGRESS)
            .setProgressBar(progress, progressColor)
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_PROGRESS, "Uploading..."))
            .setSmallIslandIcon(PIC_KEY_PROGRESS)
            .addPicture(progressPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.rounded_cloud_upload_24).setContentTitle(title).setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Circular Progress Demo"
        val text = "Showing circular progress on island"
        val progress = 75
        val progressColor = "#34C759"
        val progressPicture = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_download_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Downloading...", content = "75% complete", pictureKey = PIC_KEY_PROGRESS)
            .setBigIslandProgressCircle(PIC_KEY_PROGRESS, "", progress, progressColor, isCCW = true)
            .setSmallIslandCircularProgress(PIC_KEY_PROGRESS, progress, progressColor, isCCW = true)
            .addPicture(progressPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "Count-Up Timer"
        val text = "This demonstrates a count-up timer."
        val startTime = System.currentTimeMillis()
        val countUpTimer = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val countUpPicture = HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.rounded_timer_arrow_up_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Recording...", timer = countUpTimer, pictureKey = PIC_KEY_COUNTUP)
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP)
            .setSmallIslandIcon(PIC_KEY_COUNTUP)
            .addPicture(countUpPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val notificationId = getUniqueNotificationId()
        val title = "Simple Small Island"
        val text = "Icon on left (small), icon+text (big)."
        val openAppIntentContent = createAppOpenIntent(context, 0)
        val openAppIntentAction = createAppOpenIntent(context, 1)
        val simplePicture = HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.rounded_arrow_outward_24)
        val appOpenAction = HyperAction(key = ACTION_KEY_APP_OPEN, title = "Open App", context = context, drawableRes = R.drawable.rounded_arrow_outward_24, pendingIntent = openAppIntentAction, actionIntentType = 1, isProgressButton = false)
        val bigIslandInfo = ImageTextInfoLeft(picInfo = PicInfo(type = 1, pic = PIC_KEY_SIMPLE), textInfo = TextInfo(title = "Simple Info", content = "This is the expanded view"))
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Simple Info", content = "This is the expanded view", pictureKey = PIC_KEY_SIMPLE, actionKeys = listOf(ACTION_KEY_APP_OPEN))
            .setBigIslandInfo(bigIslandInfo)
            .setSmallIslandIcon(PIC_KEY_SIMPLE)
            .addPicture(simplePicture)
            .addAction(appOpenAction)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).setContentIntent(openAppIntentContent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val title = "App Open Demo"
        val text = "Tap or drag to open the app."
        val appOpenPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.rounded_arrow_outward_24)
        val openAppIntent = createAppOpenIntent(context, 0)
        val bigIslandInfo = ImageTextInfoLeft(picInfo = PicInfo(type = 1, pic = PIC_KEY_APP_OPEN), textInfo = TextInfo(title = "Open Demo", content = "Tap or drag"))
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .setBaseInfo(title = "App Open Demo", content = "Tap or drag to open the app", pictureKey = PIC_KEY_APP_OPEN)
            .setBigIslandInfo(bigIslandInfo)
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .addPicture(appOpenPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(getUniqueNotificationId(), notification)
    }

    fun showSplitIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val notificationId = getUniqueNotificationId()
        val title = "Split Island"
        val text = "Left & Right content on Big Island."
        val openAppIntent = createAppOpenIntent(context, 0)
        val demoPicture = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val leftInfo = ImageTextInfoLeft(picInfo = PicInfo(type = 1, pic = PIC_KEY_DEMO_ICON), textInfo = TextInfo(title = "Left"))
        val rightInfo = ImageTextInfoRight(textInfo = TextInfo(title = "Right", content = "Content"))
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Split Info", content = "Left & Right Content", pictureKey = PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = leftInfo, right = rightInfo)
            .setSmallIsland(aZone = leftInfo, bZone = rightInfo)
            .addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    fun showRightImageNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val notificationId = getUniqueNotificationId()
        val title = "Right Image Demo"
        val text = "Expanded island has an image on the right."
        val openAppIntent = createAppOpenIntent(context, 0)
        val leftPic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val rightPic = HyperPicture(PIC_KEY_RIGHT_SIDE, context, R.drawable.rounded_medication_24)
        val leftInfo = ImageTextInfoLeft(picInfo = PicInfo(type = 1, pic = PIC_KEY_DEMO_ICON), textInfo = TextInfo(title = "Left Info"))
        val rightInfo = ImageTextInfoRight(picInfo = PicInfo(type = 1, pic = PIC_KEY_RIGHT_SIDE), textInfo = TextInfo(title = "Right", content = "With Image"))
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Right Image Demo", content = "Check the expanded island", pictureKey = PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = leftInfo, right = rightInfo)
            .setSmallIsland(aZone = leftInfo, bZone = rightInfo)
            .addPicture(leftPic)
            .addPicture(rightPic)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).setContentIntent(openAppIntent).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    fun showMultiActionNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        showSupportToast(context)
        val notificationId = getUniqueNotificationId()
        val title = "Multi-Action Demo"
        val text = "This demonstrates multiple buttons."
        val stopPendingIntent = createBroadcastIntent(context, notificationId, NotificationActionReceiver.ACTION_SHOW_TOAST_ONLY, "Stop Pressed", notificationId + 1)
        val closePendingIntent = createBroadcastIntent(context, notificationId, NotificationActionReceiver.ACTION_CLICK_AND_CANCEL, "Notification Closed", notificationId + 2)
        val stopAction = HyperAction(key = ACTION_KEY_STOP_PROGRESS, title = null, context = context, drawableRes = R.drawable.rounded_pause_24, pendingIntent = stopPendingIntent, actionIntentType = 2, isProgressButton = true, progress = 40, progressColor = "#FFC700")
        val closeAction = HyperAction(key = ACTION_KEY_CLOSE_NOTIFICATION, title = "Close", pendingIntent = closePendingIntent, actionIntentType = 2, actionBgColor = "#FF3B30")
        val appPicture = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.round_smart_button_24)
        val stopPicture = HyperPicture(PIC_KEY_STOP_ICON, context, R.drawable.ic_launcher_foreground)
        val closePicture = HyperPicture(PIC_KEY_CLOSE_ICON, context, R.drawable.ic_launcher_foreground)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(title = "Multi-Action", content = "Stop or Close", pictureKey = PIC_KEY_APP_OPEN, actionKeys = listOf(ACTION_KEY_STOP_PROGRESS, ACTION_KEY_CLOSE_NOTIFICATION))
            .setBigIslandInfo(createSimpleAZone(PIC_KEY_APP_OPEN, "Actions"))
            .setSmallIslandIcon(PIC_KEY_APP_OPEN)
            .addAction(stopAction)
            .addAction(closeAction)
            .addPicture(appPicture)
            .addPicture(stopPicture)
            .addPicture(closePicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title).setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }
}