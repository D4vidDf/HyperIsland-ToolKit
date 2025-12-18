package com.d4viddf.hyperisland_kit.demo

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Icon
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.d4viddf.hyperisland_kit.demo.DemoNotificationManager.createAppOpenIntent
import com.d4viddf.hyperisland_kit.demo.DemoNotificationManager.getUniqueNotificationId
import io.github.d4viddf.hyperisland_kit.HyperAction
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.CircularProgressInfo
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoRight
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.ProgressTextInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.concurrent.TimeUnit

// --- Resource Keys ---
private const val PIC_KEY_ICON = "icon_main"
private const val PIC_KEY_COVER = "cover_image"
private const val PIC_KEY_APP_OPEN = "app_open_pic"
private const val PIC_KEY_SIMPLE = "simple"
private const val PIC_KEY_RIGHT_SIDE = "right_side"
private const val PIC_KEY_DEMO_ICON = "demo_icon"
private const val PIC_KEY_PROGRESS = "progress"
private const val PIC_KEY_COUNTUP = "countup"
private const val PIC_KEY_CAR = "car"
private const val PIC_KEY_DOT_SEL = "dot_sel"
private const val PIC_KEY_DOT_UNSEL = "dot_unsel"
private const val PIC_KEY_FLAG_SEL = "flag_sel"
private const val PIC_KEY_FLAG_UNSEL = "flag_unsel"

private const val ACTION_KEY_APP_OPEN = "app_open"
private const val ACTION_KEY_HINT = "hint"
private const val ACTION_KEY_STOP = "stop"
private const val ACTION_KEY_CLOSE = "close"
private const val ACTION_KEY_TEST_1 = "test_1"
private const val ACTION_KEY_TEST_2 = "test_2"


object DemoNotificationManager {

    // --- Helpers ---
    private fun hasNotificationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

     fun getUniqueNotificationId() = System.currentTimeMillis().toInt()

     fun createAppOpenIntent(context: Context, requestCode: Int = 0): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createRoundedBackgroundIcon(
        context: Context,
        drawableResId: Int,
        iconColor: Int,
        backgroundColor: Int,
        paddingFactor: Float = 0.25f
    ): Icon {
        val size = 128
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = backgroundColor; style = Paint.Style.FILL }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
        drawable?.let {
            val padding = (size * paddingFactor).toInt()
            it.setBounds(padding, padding, size - padding, size - padding)
            it.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            it.draw(canvas)
        }
        return Icon.createWithBitmap(bitmap)
    }

    private fun createCustomIcon(
        context: Context,
        drawableResId: Int,
        color: Color,
        paddingFactor: Float = 0.25f
    ): Icon {
        val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
            ?: return Icon.createWithResource(context, drawableResId)

        val size = 96
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val padding = (size * paddingFactor).toInt()
        drawable.setBounds(padding, padding, size - padding, size - padding)
        drawable.setColorFilter(color.toArgb(), PorterDuff.Mode.SRC_IN)
        drawable.draw(canvas)
        return Icon.createWithBitmap(bitmap)
    }

    /**
     * Creates a rounded bitmap from a drawable resource.
     * Ideal for RemoteViews ImageViews which don't support clipping/OutlineProvider.
     */
    private fun getRoundedBitmap(context: Context, drawableId: Int, cornerRadiusPx: Float): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return createBitmap(1, 1)
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        val output = createBitmap(width, height)
        val outputCanvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        outputCanvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        return output
    }



    // ============================================================================================
    // CONFIGURABLE DEMO (Restored)
    // ============================================================================================

    fun showConfigurableNotification(context: Context, timeout: Long, enableFloat: Boolean, isShowNotification: Boolean) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        val builder = HyperIslandNotification.Builder(context, "config", "Config")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .setChatInfo("Configurable", "Timeout: ${timeout}ms", PIC_KEY_ICON)
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Configured")))
            .setEnableFloat(enableFloat).setShowNotification(isShowNotification)

        if (timeout > 0) builder.setTimeout(timeout)

        notify(context, "Configurable Demo", builder)
    }

    // ============================================================================================
    // OFFICIAL TEMPLATES (1-22)
    // ============================================================================================

    // 1. Weather (BaseInfo Type 1 + PicInfo)
    fun showTemplate1_Weather(context: Context) {
        if (!hasNotificationPermission(context)) return

        val weatherIconKey = "weather_icon"
        // In a real app, use R.drawable.ic_weather_snow or similar
        val pic = HyperPicture(weatherIconKey, context, R.drawable.snow)

        val builder = HyperIslandNotification.Builder(context, "weather", "Weather")
            .addPicture(pic)
            .setBaseInfo(
                type = 1,
                title = "Heavy Snow",
                subTitle = "Red Alert",
                content = "Chaoyang District",
                subContent = "Tonight to Tomorrow",
                //pictureKey = PIC_KEY_ICON, // Removing this from BaseInfo to rely on the dedicated PicInfo component below
                colorTitle = "#FF0000"
            )
            // [ADDED] Set the dedicated Recognition Graphic Component (Image on Right)
            .setPicInfo(2,weatherIconKey)
            // Island Config
            .setSmallIsland(weatherIconKey)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = weatherIconKey),
                    textInfo = TextInfo(title = "Heavy Snow")
                )
            )

        notify(context, "Template 1: Weather", builder)
    }

    // ============================================================================================
    // OFFICIAL TEMPLATE 2 (Bill Payment)
    // ============================================================================================

    fun showTemplate2_Payment(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Icon (Usually App Icon or Service Icon)
        val iconKey = "pay_icon"
        // Using a generic icon, replace with specific 'bill' or 'phone' icon if available
        val iconPic = HyperPicture(iconKey, context, R.drawable.xiaomi)

        val builder = HyperIslandNotification.Builder(context, "payment", "Bill")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(iconPic)

            // 2. Base Info (Template 2)
            // Title: Amount / Action
            // Content: Description
            // SubTitle: App Name / Source
            .setBaseInfo(
                type = 2,
                title = "129.00",
                content = "September Phone Bill",
                subTitle = "Mi Pay",
            )

            // 3. Banner Icon (Root level PicInfo)
            // This ensures the icon appears on the right side in the notification shade (Template 2 style)
            .setPicInfo(1,iconKey)

            // 4. Island Config
            .setSmallIsland(iconKey)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = iconKey),
                    textInfo = TextInfo(title = "Payment", content = "129.00")
                )
            )

        notify(context, "Template 2: Payment", builder)
    }

    // 3. IM/Chat (ChatInfo) - Updated with Avatar, Pkg Icon, and Actions
    fun showTemplate3_Chat(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Define Assets
        // Avatar (Person)
        val avatarKey = "avatar_person"
        val avatarPic = HyperPicture(avatarKey, context, R.drawable.aidan) // Replace with R.drawable.person_avatar if available

        // Action Icons
        val callIcon = createCustomIcon(context, R.drawable.videocam, Color.White, 0.2f)

        // 2. Define Actions
        val actionAnswer = HyperAction(
            key = "answer",
            title = "Answer",
            icon = callIcon, // Icon for the button
            pendingIntent = createAppOpenIntent(context, 1),
            actionIntentType = 1,
            actionBgColor = "#34C759", // Green
            titleColor = "#FFFFFF"
        )

        // 3. Build Notification
        val builder = HyperIslandNotification.Builder(context, "chat", "Message")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(avatarPic)
            .addAction(actionAnswer)

            // ChatInfo Configuration
            .setChatInfo(
                title = "Sarah",
                content = "Incoming Video Call...",
                pictureKey = avatarKey,
                actionKeys = listOf("answer"), // Link actions to the template
            )

            // Island Configuration
            .setSmallIsland(avatarKey)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = avatarKey),
                    textInfo = TextInfo(title = "", content = "")
                ),
                right = ImageTextInfoRight(
                    type = 2,
                    textInfo = TextInfo(title = "Sarah")
                ),
                // Show actions in Big Island too
                actionKeys = listOf("answer")
            )

        notify(context, "Template 3: Chat", builder)
    }

    // 4. Taxi/Delivery (BaseInfo 2 + Icon Progress Bar)
    fun showTemplate4_TaxiQueue(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Define Assets
        // Main Icon (Left side of BaseInfo)
        val brandPic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        // Progress Bar Icons
        // 'picForward': The moving icon (Car/Bike)
        val carPic = HyperPicture(PIC_KEY_CAR,context, R.drawable.taxi)

        // 'picEnd': The destination icon (Flag/Home)
        val flagIcon = createCustomIcon(context, android.R.drawable.ic_menu_myplaces, Color(0xFF007AFF))
        val flagPic = HyperPicture(PIC_KEY_FLAG_SEL, flagIcon)

        // 'picEndUnselected': Destination icon when not reached (Gray)
        val flagGrayIcon = createCustomIcon(context, android.R.drawable.ic_menu_myplaces, Color.LightGray)
        val flagUnselPic = HyperPicture(PIC_KEY_FLAG_UNSEL, flagGrayIcon)

        val builder = HyperIslandNotification.Builder(context, "taxi", "Delivery")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            // Add all pictures to the bundle
            .addPicture(brandPic)
            .addPicture(carPic)
            .addPicture(flagPic)
            .addPicture(flagUnselPic)

            // 2. Base Info (Template 2)
            .setBaseInfo(
                type = 2,
                title = "Arriving in 5 mins",
                content = "Distance: 1.2km",
                subTitle = "Taxi",
            )

            // 3. Progress Bar (With Icons)
            .setProgressBar(
                progress = 45,
                color = "#007AFF",
                colorEnd = "#347a60",
                picForwardKey = PIC_KEY_CAR,       // The Car
                picEndKey = PIC_KEY_FLAG_SEL,      // Active Flag
                picEndUnselectedKey = PIC_KEY_FLAG_UNSEL // Inactive Flag
            )

            // 4. Island Configuration
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = PIC_KEY_ICON),
                    textInfo = TextInfo(title = "Arriving", content = "5 mins")
                )
            )

        notify(context, "Template 4: Taxi/Delivery", builder)
    }

    // 5. Dining Queue (BaseInfo 1 + Progress)
    fun showTemplate5_DiningQueue(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "dining", "Queue")
            .addPicture(pic)
            .setBaseInfo(
                type = 1,
                title = "A24",
                subTitle = "6 Tables",
                content = "Haidilao",
                subContent = "Wait 20m",
            )
            .setPicInfo(2,PIC_KEY_ICON)
            .setProgressBar(progress = 30, color = "#FF8514")
            .setBigIslandInfo(
                left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_KEY_ICON))
            )
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 5: Dining Queue", builder)
    }

    // 6. Parking (BaseInfo 2 + Progress + Colored Time)
    fun showTemplate6_Parking(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Icon (Parking P or Brand Logo)
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        val builder = HyperIslandNotification.Builder(context, "parking", "Parking")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)

            // 2. Base Info (Type 2)
            // We highlight the "Parked 10m" (subContent) in Green
            .setBaseInfo(
                type = 2,
                title = "Entered",
                content = "Charge starts 16:00",
                subContent = "Parked 10m",
                colorSubContent = "#34C759" // <--- Green Color for Time/Duration
            )
            .setPicInfo(2,PIC_KEY_ICON)

            // 3. Progress Bar (Green)
            .setProgressBar(progress = 15, color = "#34C759")

            // 4. Island Config
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = "miui.focus.pic_$PIC_KEY_ICON"),
                    textInfo = TextInfo(title = "Parking", content = "10m")
                )
            )

        notify(context, "Template 6: Parking", builder)
    }

    // 7. Upload (ChatInfo + Progress + Share + Banner Icon)
    fun showTemplate7_Upload(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Assets
        // Left: File Preview (Rounded Image)
        val fileKey = "file_preview"
        val roundedFileIcon = createRoundedBitmapIcon(context, R.drawable.starry_pplaceholder, cornerRadius = 32f)
        val filePic = HyperPicture(fileKey, roundedFileIcon)

        // Right: Status Icon (Cloud Upload)
        val statusKey = "upload_status"
        // Fixed: Use Color.parseColor for standard Android Views
        val cloudIcon = createCustomIcon(context, android.R.drawable.stat_sys_upload, Color(0xff007AFF))
        val statusPic = HyperPicture(statusKey, cloudIcon)

        val builder = HyperIslandNotification.Builder(context, "upload", "File Upload")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(filePic)
            .addPicture(statusPic)

            // 2. ChatInfo (Title/Content)
            .setChatInfo("Uploading...", "201MB / 233MB", pictureKey = fileKey)

            // 3. Banner Icon (Root PicInfo)
            .setPicInfo(2,statusKey)

            // 4. Progress Bar (Notification Shade)
            .setProgressBar(progress = 86, color = "#34C759")

            // 5. Island Configuration
            .setShareData(
                title = "design_draft.pdf",
                content = "233 MB",
                picKey = fileKey,
                shareContent = "Sharing File...",
                sharePicKey = fileKey
            )
            .setIslandConfig(priority = 2, dismissible = true)

            // 6. Island Visuals
            // Small Island: File icon with circular progress around it
            .setSmallIslandCircularProgress(pictureKey = fileKey, progress = 86, color = "#34C759")

            // Big Island:
            // Left = File Icon + Name
            // Right = Circular Progress + Percentage
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    // [FIX] Pass RAW KEY. The library adds "miui.focus.pic_" automatically.
                    picInfo = PicInfo(type = 1, pic = fileKey),
                    textInfo = TextInfo(title = "design.pdf")
                ),
                progressText = ProgressTextInfo(
                    CircularProgressInfo(progress = 86, colorReach = "#34C759", isCCW = true)
                )


            )

        notify(context, "Template 7: Upload", builder)
    }

    // 8. Coupon (ChatInfo + HintInfo with Text Button)
    fun showTemplate8_Coupon(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Assets
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        // 2. Action for the Hint (The "View" button)
        // We give it a background color (#FF8514 Orange) so it looks like a button inside the hint bar
        val actionKey = "view_coupon"
        val action = HyperAction(
            key = actionKey,
            title = "View",
            icon = null, // Text-only button
            pendingIntent = createAppOpenIntent(context),
            actionIntentType = 1,
            actionBgColor = "#FF8514", // Orange background
            titleColor = "#FFFFFF"     // White text
        )

        val builder = HyperIslandNotification.Builder(context, "coupon", "Coupon")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .addHiddenAction(action) // Must register action to use its key

            // 3. Main Content (ChatInfo)
            .setChatInfo(
                title = "Coffee House",
                content = "Buy 1 Get 1 Free on all Lattes!",
                pictureKey = PIC_KEY_ICON
            )

            // 4. Hint Info (Top Bar)
            // Displays "Coupon Available" on the left, and the "View" action button on the right
            .setHintInfo(
                title = "Coupon Available",
                actionKey = actionKey
            )

            // 5. Island Config
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = PIC_KEY_ICON), // Raw key
                    textInfo = TextInfo(title = "Coupon", content = "BOGO Free")
                )
            )

        notify(context, "Template 8: Coupon", builder)
    }

    // 9. Movie Ticket (BaseInfo 2 + Button Component 2/HintTimer)
    fun showTemplate9_Movie(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground) // Use poster if available
        val poster = HyperPicture("poster", context, R.drawable.poster)
        val action = HyperAction("code", "Code", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "movie", "Ticket")
            .addPicture(pic)
            .addPicture(poster)
            .setBaseInfo(
                type = 2,
                title = "Oppenheimer",
                subTitle = "IMAX",
                content = "19:20",
                subContent = "Row 4 Seat 6",
                pictureKey = PIC_KEY_ICON
            )
            .setBackground("poster","#6b504c", type = 2)
            .setHintTimer(frontText1 = "Start", mainText1 = "19:20", action = action)
            .setSmallIsland("poster")
        notify(context, "Template 9: Movie Ticket", builder)
    }

    // 10. Pickup (BaseInfo 2 + HintInfo with Colored Action)
    fun showTemplate10_Pickup(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Assets
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        // 2. Action for the Hint (The "Code" button)
        val actionKey = "pickup_code"
        val action = HyperAction(
            key = actionKey,
            title = "Code",
            icon = null, // Text-only button
            pendingIntent = createAppOpenIntent(context),
            actionIntentType = 1,
            actionBgColor = "#007AFF", // Blue background
            titleColor = "#FFFFFF"     // White text
        )

        val builder = HyperIslandNotification.Builder(context, "pickup", "Package")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)

            // [IMPORTANT] Register as hidden so it doesn't duplicate at the bottom
            .addHiddenAction(action)

            // 3. Base Info (Type 2 - Banner Style)
            .setBaseInfo(
                type = 2,
                title = "Ready for Pickup",
                content = "Cainiao Station",
                subTitle = "2 Packages",
                pictureKey = PIC_KEY_ICON
            )

            // 4. Banner Icon (Right side image)
            .setPicInfo(2,PIC_KEY_ICON)

            // 5. Hint Info (Top Bar) linking to the colored action
            .setHintInfo(
                title = "Pickup Code",
                actionKey = actionKey
            )

            // 6. Island Config
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = PIC_KEY_ICON), // Raw key
                    textInfo = TextInfo(title = "Pickup", content = "Code Available")
                )
            )

        notify(context, "Template 10: Pickup", builder)
    }

    // 11. Sports/Timer (HighlightInfo + Button Component 2)
    fun showTemplate11_Sports(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction("pause", "Pause", null, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "run", "Run")
            .addPicture(pic)
            .setHighlightInfo(title = "6.12 km", content = "00:30:59", subContent = "Pace 5'30\"", picKey = PIC_KEY_ICON)
            .setHintTimer(frontText1 = "Time", mainText1 = "00:30:59", action = action)
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 11: Sports", builder)
    }

    // 12. Call (ChatInfo + Custom Icon Buttons)
    fun showTemplate12_Call(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Assets
        // Avatar
        val avatarKey = "caller_avatar"
        val avatarPic = HyperPicture(avatarKey, context, R.drawable.aidan) // Replace with person image

        // 2. Generate Custom Call Buttons (Icon with Circular Background)

        // Decline Icon (Red Background + White 'X')
        val declineIcon = createRoundedBackgroundIcon(
            context,
            android.R.drawable.ic_menu_call,
            iconColor = Color.White.toArgb(),
            backgroundColor = Color(0xffFF3B30).toArgb(), // Red
            paddingFactor = 0.3f
        )

        // Answer Icon (Green Background + White Phone)
        val answerIcon = createRoundedBackgroundIcon(
            context,
            android.R.drawable.ic_menu_call,
            iconColor = Color.White.toArgb(),
            backgroundColor = Color(0xff34C759).toArgb(), // Green
            paddingFactor = 0.3f
        )

        // 3. Define Actions using the generated icons
        val actDecline = HyperAction(
            key = "decline",
            title = "Decline",
            icon = declineIcon,
            pendingIntent = createAppOpenIntent(context, 1),
            actionIntentType = 1
        )

        val actAnswer = HyperAction(
            key = "answer",
            title = "Answer",
            icon = answerIcon,
            pendingIntent = createAppOpenIntent(context, 2),
            actionIntentType = 1
        )

        // 4. Build Notification
        val builder = HyperIslandNotification.Builder(context, "call", "Call")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(avatarPic)
            .addAction(actDecline)
            .addAction(actAnswer)

            // ChatInfo: Standard Call Layout
            .setChatInfo(
                title = "John Doe",
                content = "Incoming Video Call",
                pictureKey = avatarKey
            )

            // Island Config
            .setSmallIsland(avatarKey)

            // Big Island: Avatar Left + Call Status Right
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = avatarKey),
                    textInfo = TextInfo(title = "John Doe", content = "Mobile")
                ),
                // Important: Actions appear in Big Island automatically if added via addAction
                actionKeys = listOf("decline", "answer")
            )

        notify(context, "Template 12: Call", builder)
    }

    // 13. Recording (HighlightInfo + Button Component 1)
    fun showTemplate13_Recording(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actStop = HyperAction("stop", null, context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "record", "Rec")
            .addPicture(pic).addAction(actStop)
            .setHighlightInfo("03:58", "Recording...", picKey = PIC_KEY_ICON)
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 13: Recording", builder)
    }

    // 14. Navigation (IconTextInfo)
    fun showTemplate14_Navigation(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "nav", "Nav")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Turn Right", "88 meters", "Navigating")
            .setBigIslandFixedWidthDigit(digit = 88, content = "Meters", showHighlight = true)
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 14: Navigation", builder)
    }

    // 15. Recorder (AnimTextInfo + Button Component 1)
    fun showTemplate15_Recorder(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actStop = HyperAction("stop", null, context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1)

        val builder = HyperIslandNotification.Builder(context, "recorder", "Rec")
            .setScene("recorder") // Essential for animation
            .addPicture(pic).addAction(actStop)
            .setAnimTextInfo(PIC_KEY_ICON, "Recording", "00:05", isAnimation = true)
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 15: Recorder", builder)
    }

    // 16. Code (IconTextInfo + Button Component 1)
    fun showTemplate16_Code(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actCopy = HyperAction("copy", "Copy", null, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "code", "Code")
            .addPicture(pic).addAction(actCopy)
            .setIconTextInfo(PIC_KEY_ICON, "C23JH1", "Verification Code")
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 16: Code", builder)
    }

    // 17. Promo (HighlightInfoV3)
    fun showTemplate17_Promo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val action = HyperAction("buy", "Buy", null, createAppOpenIntent(context), 1, actionBgColor = "#FF3B30", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "promo", "Sale")
            // No addAction here, it's inside V3
            .setHighlightInfoV3(primaryText = "¥4999", secondaryText = "¥5999", label = "Sale", action = action, primaryColor = "#FF0000")
            .setBaseInfo("Flash Sale", "Xiaomi 15", type = 2) // Fallback base
            .setSmallIsland(PIC_KEY_ICON)
            .addPicture(HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)) // Ensure icon exists
        notify(context, "Template 17: Promo", builder)
    }

    // 18. File Request (IconTextInfo + TextButton)
    fun showTemplate18_FileRequest(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val actAccept = HyperAction("accept", "Accept", null, createAppOpenIntent(context), 1, actionBgColor = "#007AFF", titleColor = "#FFFFFF")
        val actDecline = HyperAction("decline", "Decline", null, createAppOpenIntent(context), 1, actionBgColor = "#333333", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "file", "File")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Receive Photo", "20MB | From Xiaomi 14")
            .setTextButtons(actDecline, actAccept)
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 18: File Request", builder)
    }

    // 19. Cover Info (CoverInfo + Button Component 5)
    fun showTemplate19_Cover(context: Context) {
        if (!hasNotificationPermission(context)) return
        val cover = HyperPicture(PIC_KEY_COVER, context, R.drawable.starry_pplaceholder) // Tall image
        val action = HyperAction("book", "Book", null, createAppOpenIntent(context), 1, actionBgColor = "#007AFF", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "cover", "Concert")
            .addPicture(cover).addAction(action)
            .setBaseInfo("Concert", "Jay Chou", pictureKey = PIC_KEY_COVER) // Fallback
            .setCoverInfo(PIC_KEY_COVER, "Jay Chou", "Chengdu", "Feb 24")
            .setHintAction("26:00 Remaining", action = action) // Reuse HintAction for Comp 5
            .setSmallIsland(PIC_KEY_COVER)
        notify(context, "Template 19: Cover Info", builder)
    }

    // 20. Data Usage (IconTextInfo + Progress)
    fun showTemplate20_Data(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "data", "Data")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Thailand 1GB", "Left: 2 Days", "199MB Left")
            .setProgressBar(progress = 20, color = "#007AFF")
            .setSmallIslandCircularProgress(PIC_KEY_ICON, 20, "#007AFF")
        notify(context, "Template 20: Data Usage", builder)
    }

    // 21. Game Download (ChatInfo + Progress)
    fun showTemplate21_Game(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "game", "Download")
            .addPicture(pic)
            .setChatInfo("Game Launch", "Downloading assets...", PIC_KEY_ICON)
            .setProgressBar(progress = 45, color = "#FF6700")
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 21: Game Download", builder)
    }

    // 22. IoT (IconTextInfo + Progress)
    fun showTemplate22_IoT(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "iot", "IoT")
            .addPicture(pic)
            .setIconTextInfo(PIC_KEY_ICON, "Washing", "30 mins left", "Balcony Washer")
            .setProgressBar(progress = 60, color = "#007AFF")
            .setSmallIsland(PIC_KEY_ICON)
        notify(context, "Template 22: IoT", builder)
    }

    // ============================================================================================
    // ADVANCED CUSTOMIZATION DEMOS (Restored)
    // ============================================================================================

    fun showRawColoredTextButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Accept", null, createAppOpenIntent(context), 1, actionBgColor = "#34C759", titleColor = "#FFFFFF")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Reject", null, createAppOpenIntent(context), 1, actionBgColor = "#FF3B30", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "textbtn", "Buttons")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Text Buttons", "Colored actions", pictureKey = PIC_KEY_ICON, type = 2)
            .setTextButtons(action1, action2)
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Buttons")))

        notify(context, "Text Buttons", builder)
    }

    fun showRawIconButtons(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)

        // Use custom icon creation if needed, or resources
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Prev", context, android.R.drawable.ic_media_rew, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Next", context, android.R.drawable.ic_media_ff, createAppOpenIntent(context), 1, actionBgColor = "#E0E0E0")

        val builder = HyperIslandNotification.Builder(context, "icons", "Icons")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Music Control", "Icon buttons", pictureKey = PIC_KEY_ICON, type = 2)
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Music")))

        notify(context, "Icon Buttons", builder)
    }

    fun showRawProgressAndColorButton(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val action1 = HyperAction(ACTION_KEY_TEST_1, "Progress", context, R.drawable.rounded_pause_24, createAppOpenIntent(context), 1, isProgressButton = true, progress = 65, progressColor = "#34C759")
        val action2 = HyperAction(ACTION_KEY_TEST_2, "Close", null, createAppOpenIntent(context), 1, actionBgColor = "#FF6700", titleColor = "#FFFFFF")

        val builder = HyperIslandNotification.Builder(context, "mix", "Mix")
            .addPicture(pic).addAction(action1).addAction(action2)
            .setBaseInfo("Download", "Mix Buttons", pictureKey = PIC_KEY_ICON, type = 2)
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Download")))

        notify(context, "Mix Buttons", builder)
    }

    fun showRawBgInfo(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "bg", "Bg")
            .addPicture(pic)
            .setBaseInfo("Custom BG", "Colored background", pictureKey = PIC_KEY_ICON, type = 2)
            .setBackground(color = "#E6F0FF")
            .setSmallIsland(PIC_KEY_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_ICON"), textInfo=TextInfo(title="Custom BG")))

        notify(context, "Bg Info", builder)
    }

    // ============================================================================================
    // STANDARD DEMOS (Restored for Compatibility)
    // ============================================================================================

    fun showAppOpenNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.rounded_arrow_outward_24)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "App Open")
            .setSmallWindowTarget("${context.packageName}.MainActivity")
            .addPicture(pic)
            .setBaseInfo(title = "App Open Demo", content = "Tap or drag to open", pictureKey = PIC_KEY_APP_OPEN)
            .setBigIslandInfo(left = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_$PIC_KEY_APP_OPEN"), textInfo = TextInfo(title = "Open Demo")))
            .setSmallIsland(PIC_KEY_APP_OPEN)
        notify(context, "App Open Demo", builder)
    }

    fun showChatNotification(context: Context) {
        showTemplate3_Chat(context) // Alias to template 3
    }

    fun showSimpleSmallIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val simplePicture = HyperPicture(PIC_KEY_SIMPLE, context, R.drawable.rounded_arrow_outward_24)
        val appOpenAction = HyperAction(ACTION_KEY_APP_OPEN, "Open", context, R.drawable.rounded_arrow_outward_24, createAppOpenIntent(context), 1)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Simple")
            .setChatInfo(title = "Simple Info", content = "Expanded view", pictureKey = PIC_KEY_SIMPLE, actionKeys = listOf(ACTION_KEY_APP_OPEN))
            .setSmallIsland(PIC_KEY_SIMPLE)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_SIMPLE"), textInfo=TextInfo(title="Simple Info")))
            .addPicture(simplePicture).addAction(appOpenAction)
        notify(context, "Simple Island", builder)
    }

    fun showRightImageNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val leftPic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val rightPic = HyperPicture(PIC_KEY_RIGHT_SIDE, context, R.drawable.rounded_medication_24)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Right Img")
            .setChatInfo("Right Image", "Check island", PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Left")),
                right = ImageTextInfoRight(type=2, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_RIGHT_SIDE"), textInfo=TextInfo(title="Right"))
            )
            .setSmallIsland(PIC_KEY_RIGHT_SIDE)
            .addPicture(leftPic).addPicture(rightPic)
        notify(context, "Right Image", builder)
    }

    fun showSplitIslandNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Split")
            .setChatInfo("Split Info", "Left & Right", PIC_KEY_DEMO_ICON)
            .addPicture(pic)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Left")),
                right = ImageTextInfoRight(type=2, textInfo=TextInfo(title="Right"))
            )
            .setSmallIsland(PIC_KEY_DEMO_ICON)
        notify(context, "Split Island", builder)
    }

    fun showHintInfoNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val action = HyperAction(ACTION_KEY_HINT, "View", null, createAppOpenIntent(context), 1)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Hint")
            .setChatInfo("Main Content", "Look up", PIC_KEY_DEMO_ICON)
            .setHintInfo("2 New Messages", ACTION_KEY_HINT)
            .addPicture(pic).addAction(action)
            .setSmallIsland(PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Main Content")))
        notify(context, "Hint Info", builder)
    }

    fun showMultiNodeProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "demoApp", "Nodes")
            .setBaseInfo("Processing", "Order is being prepared", pictureKey = PIC_KEY_DEMO_ICON, type = 2)
            .setMultiProgress("Step 2 of 4", 2, "#34C759", 3)
            .setSmallIsland(PIC_KEY_DEMO_ICON)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_DEMO_ICON"), textInfo=TextInfo(title="Processing")))
            .addPicture(pic)
        notify(context, "Multi Node", builder)
    }

    fun showProgressBarNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val picMain = HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_upload_24)
        val picCar = HyperPicture(PIC_KEY_CAR, context, R.drawable.rounded_arrow_outward_24)
        val picDot = HyperPicture(PIC_KEY_DOT_SEL, context, R.drawable.round_smart_button_24)
        val picDotUn = HyperPicture(PIC_KEY_DOT_UNSEL, context, R.drawable.ic_launcher_foreground)
        val picFlag = HyperPicture(PIC_KEY_FLAG_SEL, context, R.drawable.ic_launcher_foreground)
        val picFlagUn = HyperPicture(PIC_KEY_FLAG_UNSEL, context, R.drawable.ic_launcher_foreground)
        val builder = HyperIslandNotification.Builder(context, "progress", "Progress")
            .addPicture(picMain).addPicture(picCar).addPicture(picDot).addPicture(picDotUn).addPicture(picFlag).addPicture(picFlagUn)
            .setChatInfo("Delivery", "Arriving...", PIC_KEY_PROGRESS)
            .setProgressBar(70, "#FF8514", null, PIC_KEY_CAR, PIC_KEY_DOT_SEL, PIC_KEY_DOT_UNSEL, PIC_KEY_FLAG_SEL, PIC_KEY_FLAG_UNSEL)
            .setSmallIsland(PIC_KEY_PROGRESS)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_PROGRESS"), textInfo=TextInfo(title="Arriving")))
        notify(context, "Icon Progress", builder)
    }

    fun showCircularProgressNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "Circular Progress Demo"
        val text = "Showing circular progress on island"
        val progress = 75
        val progressColor = "#34C759"
        val progressPicture =
            HyperPicture(PIC_KEY_PROGRESS, context, R.drawable.rounded_cloud_download_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Downloading...",
                content = "75% complete",
                pictureKey = PIC_KEY_PROGRESS
            ).setBigIslandProgressCircle(
                PIC_KEY_PROGRESS,
                "",
                progress,
                progressColor,
                isCCW = true
            ).setSmallIslandCircularProgress(PIC_KEY_PROGRESS, progress, progressColor, isCCW = true)
            .addPicture(progressPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountdownNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val title = "Countdown Notification"
        val text = "This demonstrates a countdown timer."
        val countdownTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val countdownTimer =
            TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val demoPicture =
            HyperPicture(PIC_KEY_DEMO_ICON, context, R.drawable.rounded_timer_arrow_down_24)
        val hyperIslandBuilder = HyperIslandNotification.Builder(context, "demoApp", title)
            .setChatInfo(
                title = "Pizza in oven",
                timer = countdownTimer,
                pictureKey = PIC_KEY_DEMO_ICON
            ).setBigIslandCountdown(countdownTime, PIC_KEY_DEMO_ICON)
            .setSmallIsland(PIC_KEY_DEMO_ICON).addPicture(demoPicture)
        val resourceBundle = hyperIslandBuilder.buildResourceBundle()
        val jsonParam = hyperIslandBuilder.buildJsonParam()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.rounded_timer_arrow_down_24).setContentTitle(title)
            .setContentText(text).addExtras(resourceBundle).build()
        notification.extras.putString("miui.focus.param", jsonParam)
        context.getSystemService(NotificationManager::class.java)
            .notify(getUniqueNotificationId(), notification)
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val pic = HyperPicture(PIC_KEY_COUNTUP, context, R.drawable.rounded_timer_arrow_up_24)

        val startTime = System.currentTimeMillis()
        val counUpTimer = TimerInfo(1, System.currentTimeMillis(), System.currentTimeMillis(),
            System.currentTimeMillis())
        val builder = HyperIslandNotification.Builder(context, "timer", "Timer")
            .addPicture(pic)
            .setChatInfo("Timer", "Count Up", PIC_KEY_COUNTUP, timer = counUpTimer)
            .setSmallIsland(PIC_KEY_COUNTUP)
            .setBigIslandCountUp(startTime, PIC_KEY_COUNTUP)
        notify(context, "CountUp", builder)
    }

    fun showMultiActionNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        val stopAction = HyperAction(ACTION_KEY_STOP, null, null, createAppOpenIntent(context), 2, isProgressButton = true, progress = 10, colorReach = "#D9E0FA")
        val closeAction = HyperAction(ACTION_KEY_CLOSE, "Close", null, createAppOpenIntent(context), 2, actionBgColor = "#FF3B30")
        val pic = HyperPicture(PIC_KEY_APP_OPEN, context, R.drawable.round_smart_button_24)
        val builder = HyperIslandNotification.Builder(context, "multi", "Actions")
            .addPicture(pic).addAction(stopAction).addAction(closeAction)
            .setChatInfo("Actions", "Stop or Close", PIC_KEY_APP_OPEN, actionKeys = listOf(ACTION_KEY_STOP, ACTION_KEY_CLOSE))
            .setSmallIsland(PIC_KEY_APP_OPEN)
            .setBigIslandInfo(left = ImageTextInfoLeft(type=1, picInfo=PicInfo(type=1, pic="miui.focus.pic_$PIC_KEY_APP_OPEN"), textInfo=TextInfo(title="Actions")))
        notify(context, "Multi Action", builder)
    }


    // ============================================================================================
    // FOCUS DIY (CUSTOM VIEW) DEMO - UPDATED TO USE LIBRARY
    // ============================================================================================

    fun showFocusDiyNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Prepare RemoteViews
        val remoteView = RemoteViews(context.packageName, R.layout.layout_focus_diy)
        remoteView.setTextViewText(R.id.title, "Library Integration")
        remoteView.setTextViewText(R.id.text, "This uses HyperIslandNotification.setCustomRemoteView()")

        // 2. Prepare Icon
        val icon = Icon.createWithResource(context, R.drawable.ic_launcher_foreground)

        // 3. Build using Library API
        val builder = HyperIslandNotification.Builder(context, "diy_test", "DIY Test")
            .setTickerIcon(icon)
            .setCustomRemoteView(remoteView) // This enables Custom Mode automatically
            .setEnableFloat(true)
            .setTimeout(5000)

        // 4. Notify using Custom Extras
        notifyCustom(context, builder)
    }

    // ============================================================================================
    // MUSIC PLAYER DEMO (FOCUS DIY) - UPDATED TO USE LIBRARY
    // ============================================================================================

    fun showMusicPlayerDemo(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Setup Custom Banner (RemoteView)
        // This is what shows in the Notification Shade
        val remoteView = RemoteViews(context.packageName, R.layout.layout_focus_music_m3)

        remoteView.setTextViewText(R.id.tv_title, "California Dreamin'")
        remoteView.setTextViewText(R.id.tv_artist, "Valley Of Wolves")
        remoteView.setTextViewText(R.id.tv_time_current, "0:45")
        remoteView.setTextViewText(R.id.tv_time_total, "3:15")
        remoteView.setProgressBar(R.id.progress_bar, 100, 20, false)

        val coverBitmap = getRoundedBitmap(context, R.drawable.cover_example, 32f)
        remoteView.setImageViewBitmap(R.id.iv_cover, coverBitmap)

        remoteView.setOnClickPendingIntent(R.id.btn_play, getActionIntent(context, "ACTION_PLAY"))
        remoteView.setOnClickPendingIntent(R.id.btn_next, getActionIntent(context, "ACTION_NEXT"))
        remoteView.setOnClickPendingIntent(R.id.btn_prev, getActionIntent(context, "ACTION_PREV"))

        // 2. Setup Resources for Island
        val icon = Icon.createWithResource(context, R.drawable.ic_launcher_foreground)
        val coverKey = "cover_art"
        val coverPic = HyperPicture(coverKey, context, R.drawable.cover_example) // Use standard drawable for Island

        // 3. Build Notification
        val builder = HyperIslandNotification.Builder(context, "music_m3", "M3 Music")
            .setTickerIcon(icon)
            .addPicture(coverPic)

            // --- A. Custom Notification Banner ---
            .setCustomRemoteView(remoteView)

            // --- B. Standard Island Configuration ---
            // We use the standard setters here. The builder automatically puts them
            // into the 'param_island' JSON field inside the custom payload.

            // Small Island: Icon + Circular Progress
            .setSmallIslandCircularProgress(
                pictureKey = coverKey,
                progress = 20,
                color = "#D0BCFF",
                isCCW = false
            )

            // Big Island: Cover Art (Left) + Title/Artist (Right)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = coverKey),
                    textInfo = TextInfo(title = "Cal'", content = "Valley Of Wolves")
                )
            )
            // Config
            .setEnableFloat(true)
            .setHideDeco(true)
            .setTimeout(10000)

        notifyCustom(context, builder)
    }

    // --- Helpers ---

    private fun notify(context: Context, title: String, builder: HyperIslandNotification) {
        val notificationId = getUniqueNotificationId()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentIntent(createAppOpenIntent(context))
            .addExtras(builder.buildResourceBundle()) // Standard Bundle
            .build()

        // Add JSON ParamV2
        notification.extras.putString("miui.focus.param", builder.buildJsonParam())

        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    /**
     * Specialized notify helper for Custom View mode.
     * Uses buildCustomExtras() instead of buildResourceBundle() + JSON string.
     */
    private fun notifyCustom(context: Context, builder: HyperIslandNotification) {
        val notificationId = getUniqueNotificationId()

        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Custom View") // Fallback title
            .setContentIntent(createAppOpenIntent(context))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Good practice for custom views
            // .setCustomContentView(remoteView) // Optional: If you want standard shade to match
            .addExtras(builder.buildCustomExtras()) // [CRITICAL] Injects RV + JSON + Pics
            .build()

        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }
}

    // Helper to create simple action intents (Broadcasts)
    private fun getActionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Helper for raw notification notify
    private fun notify(context: Context, id: Int, notification: Notification) {
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }

    // --- Helper ---
    private fun notify(context: Context, title: String, builder: HyperIslandNotification) {
        val notificationId = getUniqueNotificationId()
        val notification = NotificationCompat.Builder(context, DemoApplication.DEMO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(title)
            .setContentIntent(createAppOpenIntent(context)).addExtras(builder.buildResourceBundle()).build()
        notification.extras.putString("miui.focus.param", builder.buildJsonParam())
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }


/**
 * Creates an Icon from a drawable resource with rounded corners.
 * Ideal for File Previews, Album Art, or User Avatars.
 */
private fun createRoundedBitmapIcon(context: Context, drawableResId: Int, cornerRadius: Float = 24f): Icon {
    val drawable = ContextCompat.getDrawable(context, drawableResId)
        ?: return Icon.createWithResource(context, drawableResId)

    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

    // 1. Draw source drawable to bitmap
    val sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val sourceCanvas = Canvas(sourceBitmap)
    drawable.setBounds(0, 0, sourceCanvas.width, sourceCanvas.height)
    drawable.draw(sourceCanvas)

    // 2. Draw rounded rect with bitmap shader
    val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val outputCanvas = Canvas(outputBitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        shader = BitmapShader(sourceBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    outputCanvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

    return Icon.createWithBitmap(outputBitmap)
}