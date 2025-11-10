package com.d4viddf.hyperisland_kit

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.d4viddf.hyperisland_kit.models.BaseInfo
import com.d4viddf.hyperisland_kit.models.BigIslandArea
import com.d4viddf.hyperisland_kit.models.ChatInfo
import com.d4viddf.hyperisland_kit.models.CircularProgressInfo
import com.d4viddf.hyperisland_kit.models.CombinePicInfo
import com.d4viddf.hyperisland_kit.models.HyperActionRef
import com.d4viddf.hyperisland_kit.models.HyperIslandPayload
import com.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import com.d4viddf.hyperisland_kit.models.ImageTextInfoRight
import com.d4viddf.hyperisland_kit.models.ParamIsland
import com.d4viddf.hyperisland_kit.models.ParamV2
import com.d4viddf.hyperisland_kit.models.PicInfo
import com.d4viddf.hyperisland_kit.models.ProgressInfo
import com.d4viddf.hyperisland_kit.models.ProgressTextInfo
import com.d4viddf.hyperisland_kit.models.SameWidthDigitInfo
import com.d4viddf.hyperisland_kit.models.SmallIslandArea
import com.d4viddf.hyperisland_kit.models.SmallWindowInfo
import com.d4viddf.hyperisland_kit.models.TextInfo
import com.d4viddf.hyperisland_kit.models.TimerInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri

/**
 * Represents a single clickable action in a HyperIsland notification.
 *
 * @property key A unique string ID for this action. This key **must** match a key used in
 * a builder method (e.g., `setChatInfo(actionKey = ...)`).
 * @property title The text to display on the button.
 * @property icon The [Icon] to display on the button.
 * @property pendingIntent The [PendingIntent] to fire when the action is clicked.
 * @property isProgressButton Set to `true` if this action should be rendered as a circular progress button
 * (like in `chatInfo` actions).
 * @property progress The progress value (0-100) if [isProgressButton] is `true`.
 * @property progressColor The hex color string (e.g., "#FF8514") for the progress bar.
 */
data class HyperAction(
    val key: String,
    val title: CharSequence,
    val icon: Icon,
    val pendingIntent: PendingIntent,
    val isProgressButton: Boolean = false,
    val progress: Int = 0,
    val progressColor: String? = null
)

/**
 * Represents a single image or icon resource for a HyperIsland notification.
 *
 * @property key A unique string ID for this picture. This key **must** match a key used in
 * a builder method (e.g., `setChatInfo(pictureKey = ...)`).
 * @property icon The [Icon] to use. This should be a full-color bitmap, not a monochrome vector.
 */
data class HyperPicture(
    val key: String,
    val icon: Icon
) {
    /**
     * Secondary constructor to create a [HyperPicture] directly from a vector drawable resource.
     *
     * @param key A unique string ID for this picture.
     * @param context The application context.
     * @param drawableRes The resource ID of the vector drawable (e.g., `R.drawable.ic_my_icon`).
     */
    constructor(key: String, context: Context, drawableRes: Int) : this(
        key = key,
        icon = Icon.createWithBitmap(
            getBitmapFromVectorDrawable(
                context,
                drawableRes
            )
        )
    )

    companion object {
        /**
         * Converts a vector drawable resource into a [Bitmap] suitable for a HyperIsland [Icon].
         */
        private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap?{
            return ContextCompat.getDrawable(context, drawableId)?.let { drawable ->
                val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }
    }
}

/**
 * Main builder class for creating Xiaomi HyperIsland notifications.
 *
 * This class provides a fluent API to construct the complex JSON payload and [Bundle]
 * required by the HyperIsland system.
 *
 * Usage:
 * ```
 * val extras = HyperIslandNotification
 * .Builder(context, "myBusiness", "Ticker Text")
 * .setChatInfo(...)
 * .setSmallIslandIcon(...)
 * .setBigIslandCountdown(...)
 * .addPicture(...)
 * .buildExtras()
 *
 * val notification = NotificationCompat.Builder(context, CHANNEL_ID)
 * .setSmallIcon(R.drawable.ic_stat_notify)
 * .setContentTitle("Title")
 * .setContentText("Text")
 * .addExtras(extras) // Add the HyperIsland payload here
 * .build()
 * ```
 *
 * @param context The application context.
 * @param businessName A string identifying your app (e.g., "PillPal", "MyDemoApp").
 * @param ticker A short text displayed when the notification first appears.
 */
class HyperIslandNotification private constructor(
    private val context: Context,
    private val businessName: String,
    private val ticker: String
) {
    private var targetPage: String? = null
    private var chatInfo: ChatInfo? = null
    private var baseInfo: BaseInfo? = null
    private var paramIsland: ParamIsland? = null
    private var progressBar: ProgressInfo? = null

    private val actions = mutableListOf<HyperAction>()
    private val pictures = mutableListOf<HyperPicture>()

    /**
     * Configure Json to:
     * 1. encodeDefaults = true: (e.g., "type": 1)
     * 2. explicitNulls = false: Omit properties that are null.
     */
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonSerializer = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    // --- Public Builder Methods ---

    /**
     * Sets the deep-link target page for the small window "drag-to-open" gesture.
     *
     * @param fullyQualifiedActivityName The fully qualified class name of the Activity to open
     * (e.g., "com.mydomain.myapp.MainActivity").
     */
    fun setSmallWindowTarget(fullyQualifiedActivityName: String) = apply {
        this.targetPage = fullyQualifiedActivityName
    }

    /**
     * Sets the expanded notification panel to the "Chat" style. [cite: 1422-1426]
     * This is mutually exclusive with [setBaseInfo].
     *
     * @param title The main title of the chat.
     * @param content The secondary text content. (Will be ignored if [timer] is set).
     * @param pictureKey The [HyperPicture.key] of an icon to show.
     * @param timer A [TimerInfo] object to display a count-up or count-down. [cite: 266]
     * @param actionKey The [HyperAction.key] of an action to show (often a progress button).
     */
    fun setChatInfo(
        title: String,
        content: String? = null,
        pictureKey: String? = null,
        timer: TimerInfo? = null,
        actionKey: String? = null
    ) = apply {
        val actionRef = actionKey?.let { key ->
            actions.firstOrNull { it.key == key }?.toActionRef()
        }

        this.chatInfo = ChatInfo(
            title = title,
            content = if (timer != null) null else content,
            picFunction = pictureKey,
            timerInfo = timer,
            actions = if (actionRef != null) listOf(actionRef) else null
        )
    }

    /**
     * Sets the expanded notification panel to the "Base" style. [cite: 1383-1386]
     * This is mutually exclusive with [setChatInfo].
     *
     * @param title The main title text.
     * @param content The secondary text content.
     * @param subTitle Optional text between title and content.
     * @param pictureKey The [HyperPicture.key] of an icon to show.
     * @param type The template type (1 or 2).
     */
    fun setBaseInfo(
        title: String,
        content: String,
        subTitle: String? = null,
        pictureKey: String? = null,
        type: Int = 1
    ) = apply {
        this.baseInfo = BaseInfo(
            type = type,
            title = title,
            subTitle = subTitle,
            content = content,
            picFunction = pictureKey
        )
    }

    /**
     * Sets the summary (small island) state to a text+icon (A-Zone) and/or text+icon (B-Zone).
     *
     * @param aZone The left-side (A-Zone) component (required). [cite: 1706]
     * @param bZone The right-side (B-Zone) component (optional). [cite: 1709]
     */
    fun setSmallIsland(aZone: ImageTextInfoLeft, bZone: ImageTextInfoRight?) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(imageTextInfoLeft = aZone, imageTextInfoRight = bZone)
        )
    }

    /**
     * Sets the summary (small island) state to show only an icon. [cite: 1984-1990]
     *
     * @param picKey The [HyperPicture.key] of the icon to display.
     */
    fun setSmallIslandIcon(picKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(
                picInfo = PicInfo(type = 1, pic = picKey)
            )
        )
    }

    /**
     * Sets the summary (small island) state to show an icon and circular progress.
     * (Template 5) [cite_start][cite: 2013-2014]
     *
     * @param pictureKey The [HyperPicture.key] of the icon to display.
     * @param progress The progress percentage (0-100).
     * @param color The hex color string (e.g., "#FF8514").
     * @param isCCW Set true for counter-clockwise progress.
     */
    fun setSmallIslandCircularProgress(
        pictureKey: String,
        progress: Int,
        color: String? = null,
        isCCW: Boolean = false
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

        val progressComponent = CombinePicInfo(
            picInfo = PicInfo(type = 1, pic = pictureKey),
            progressInfo = CircularProgressInfo(
                progress = progress,
                colorReach = color,
                isCCW = isCCW
            )
        )

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            smallIslandArea = SmallIslandArea(
                combinePicInfo = progressComponent
            )
        )
    }

    /**
     * Sets the expanded (big island) state to show a simple info panel.
     * (e.g., Template 1: Text + Icon) [cite_start][cite: 362-368]
     *
     * @param info The [ImageTextInfoLeft] component to display.
     */
    fun setBigIslandInfo(info: ImageTextInfoLeft) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = info,
                sameWidthDigitInfo = null // Ensure this is null
            )
        )
    }

    /**
     * Sets the expanded (big island) state to show a circular progress bar.
     * (Template 5) [cite_start][cite: 30-39]
     *
     * @param pictureKey The [HyperPicture.key] of the icon to display on the left.
     * @param title The title text to display on the left.
     * @param progress The progress percentage (0-100).
     * @param color The hex color string (e.g., "#FF8514").
     * @param isCCW Set true for counter-clockwise progress.
     */
    fun setBigIslandProgressCircle(
        pictureKey: String,
        title: String,
        progress: Int,
        color: String? = null,
        isCCW: Boolean = false
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

        // 1. Create the left-side component with the title [cite: 362-368]
        val leftInfo = ImageTextInfoLeft(
            type = 1,
            picInfo = PicInfo(type = 1, pic = pictureKey),
            textInfo = TextInfo(title = title, content = null)
        )

        // 2. Create the progress component [cite: 30-39]
        val progressComponent = ProgressTextInfo(
            progressInfo = CircularProgressInfo(
                progress = progress,
                colorReach = color,
                isCCW = isCCW
            ),
            textInfo = null
        )

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                progressTextInfo = progressComponent
            )
        )
    }

    /**
     * Sets the expanded (big island) state to show a countdown timer.
     *
     * @param countdownTime The target time (in millis) for a countdown timer.
     * @param pictureKey The [HyperPicture.key] of the icon to display on the left of the timer.
     */
    fun setBigIslandCountdown(countdownTime: Long, pictureKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(
            timerType = -1, // -1 for countdown
            timerWhen = countdownTime,
            timerTotal = System.currentTimeMillis(),
            timerSystemCurrent = System.currentTimeMillis()
        )

        val leftInfo = ImageTextInfoLeft(
            type = 1,
            picInfo = PicInfo(type = 1, pic = pictureKey),
            textInfo = TextInfo(title = "", content = null)
        )

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo)
            )
        )
    }

    /**
     * Sets the expanded (big island) state to show a count-up timer.
     *
     * @param startTime The start time (in millis) for a count-up timer.
     * @param pictureKey The [HyperPicture.key] of the icon to display on the left of the timer.
     */
    fun setBigIslandCountUp(startTime: Long, pictureKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(
            timerType = 1, // 1 for count-up
            timerWhen = startTime,
            timerTotal = startTime,
            timerSystemCurrent = System.currentTimeMillis()
        )

        val leftInfo = ImageTextInfoLeft(
            type = 1,
            picInfo = PicInfo(type = 1, pic = pictureKey),
            textInfo = TextInfo(title = "", content = null)
        )

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo)
            )
        )
    }


    /**
     * Adds a linear progress bar to the expanded notification panel.
     * (Template 21) [cite_start][cite: 1244-1248]
     *
     * @param progress The progress percentage (0-100).
     * @param color The hex color string (e.g., "#FF8514").
     */
    fun setProgressBar(progress: Int, color: String? = null) = apply {
        this.progressBar = ProgressInfo(progress, color)
    }

    /**
     * Registers a [HyperAction] to be included in the notification's action [Bundle].
     * You must add any action you reference by key in other builder methods.
     */
    fun addAction(action: HyperAction) = apply {
        this.actions.add(action)
    }

    /**
     * Registers a [HyperPicture] to be included in the notification's picture [Bundle].
     * You must add any picture you reference by key in other builder methods.
     */
    fun addPicture(picture: HyperPicture) = apply {
        this.pictures.add(picture)
    }

    /**
     * Builds the final [Bundle] to be attached to a [Notification.Builder]
     * using [Notification.Builder.addExtras].
     *
     * @return A [Bundle] containing `miui.focus.param` (JSON), `miui.focus.actions`,
     * and `miui.focus.pics`.
     */
    fun buildExtras(): Bundle {
        val bundle = Bundle()
        if (!isSupported(context)) return bundle

        // 1. Build the JSON payload
        val paramV2 = ParamV2(
            business = businessName,
            ticker = ticker,
            smallWindowInfo = targetPage?.let { SmallWindowInfo(it) },
            chatInfo = this.chatInfo,
            baseInfo = this.baseInfo,
            paramIsland = this.paramIsland,
            actions = this.actions.map { it.toActionRef() }.ifEmpty { null },
            progressInfo = this.progressBar
        )
        val payload = HyperIslandPayload(paramV2)

        // Use the custom serializer
        val jsonString = jsonSerializer.encodeToString(payload)

        // Log with a unique ID to make it searchable
        Log.d("HyperIsland", "ID [${payload.hashCode()}] JSON: $jsonString")

        bundle.putString("miui.focus.param", jsonString)

        // 2. Build the Actions Bundle
        val actionsBundle = Bundle()
        actions.forEach {
            val notificationAction = Notification.Action.Builder(it.icon, it.title, it.pendingIntent).build()
            actionsBundle.putParcelable(it.key, notificationAction)
        }
        bundle.putBundle("miui.focus.actions", actionsBundle)

        // 3. Build the Pics Bundle
        val picsBundle = Bundle()
        pictures.forEach {
            picsBundle.putParcelable(it.key, it.icon)
        }
        bundle.putBundle("miui.focus.pics", picsBundle)

        return bundle
    }

    // --- Private Helpers ---

    private fun HyperAction.toActionRef(): HyperActionRef {
        return HyperActionRef(
            type = if (isProgressButton) 2 else 1,
            action = this.key,
            actionIntent = this.key,
            progressInfo = if (isProgressButton) ProgressInfo(progress, progressColor) else null,
            actionTitle = this.title.toString()
        )
    }

    companion object {
        /**
         * The entry point for the [HyperIslandNotification] builder.
         *
         * @param context The application context.
         * @param businessName A string identifying your app (e.g., "PillPal", "MyDemoApp").
         * @param ticker A short text displayed when the notification first appears.
         */
        fun Builder(context: Context, businessName: String, ticker: String): HyperIslandNotification {
            return HyperIslandNotification(context, businessName, ticker)
        }

        /**
         * Checks if the current device supports HyperIsland notifications.
         *
         * @return `true` if the device is a Xiaomi device, has permission, and supports the feature.
         */
        fun isSupported(context: Context): Boolean {
            return isXiaomiDevice() && hasFocusPermission(context) && isSupportIsland()
        }

        private fun isXiaomiDevice(): Boolean {
            return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
        }

        private fun hasFocusPermission(context: Context): Boolean {
            return try {
                val uri = "content://miui.statusbar.notification.public".toUri()
                val extras = Bundle()
                extras.putString("package", context.packageName)
                val bundle = context.contentResolver.call(uri, "canShowFocus", null, extras)
                bundle?.getBoolean("canShowFocus", false) ?: false
            } catch (e: Exception) {
                false
            }
        }

        @SuppressLint("PrivateApi")
        private fun isSupportIsland(): Boolean {
            return try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method = clazz.getDeclaredMethod("getBoolean", String::class.java, Boolean::class.java)
                method.invoke(null, "persist.sys.feature.island", false) as Boolean
            } catch (e: Exception) {
                false
            }
        }
    }
}