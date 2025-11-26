package io.github.d4viddf.hyperisland_kit

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.github.d4viddf.hyperisland_kit.models.* // Import all your models
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import androidx.core.net.toUri

/**
 * The mandatory prefix required by the Xiaomi HyperOS framework for all action keys.
 * This is automatically prepended by the builder to ensure system compatibility.
 */
private const val ACTION_PREFIX = "miui.focus.action_"

/**
 * The mandatory prefix required by the Xiaomi HyperOS framework for all picture keys.
 * This is automatically prepended by the builder.
 */
private const val PIC_PREFIX = "miui.focus.pic_"

/**
 * Represents a single clickable action button in a HyperIsland notification.
 *
 * This class defines the visual appearance and the behavior (Intent) of a button.
 * It supports three modes:
 * 1. **Standard (Type 0):** Icon + Text (or Icon only).
 * 2. **Progress (Type 1):** Icon surrounded by a circular progress ring.
 * 3. **Text-Only (Type 2):** Text with a colored background (no icon).
 *
 * @property key A unique string ID for this action (e.g., "stop_timer"). Used to map the click event.
 * @property title The text to display on the button. Required for Type 0 and Type 2. Ignored for Type 1.
 * @property icon The [Icon] to display. Required for Type 0 and Type 1. Must be null for Type 2.
 * @property pendingIntent The [PendingIntent] to fire when the action is clicked.
 * @property actionIntentType The type of component the Intent targets. **CRITICAL**:
 * - `1`: Activity (via `PendingIntent.getActivity`)
 * - `2`: Broadcast (via `PendingIntent.getBroadcast`)
 * - `3`: Service (via `PendingIntent.getService`)
 * @property isProgressButton Set to `true` to render this as a Type 1 circular progress button.
 * @property progress The current progress value (0-100) for the circular ring. Only used if [isProgressButton] is true.
 * @property progressColor The color of the progress ring (e.g., "#FF8514"). Only used if [isProgressButton] is true.
 * @property actionBgColor The background color of the button (e.g., "#FF3B30"). Primarily used for Type 2 (Text-only) buttons.
 * @property isCCW If `true`, the circular progress fills Counter-Clockwise. Default is `false` (Clockwise).
 * @property colorReach Alias for [progressColor]. Specifies the color of the "reached" (filled) portion of the progress ring.
 */
data class HyperAction(
    val key: String,
    val title: CharSequence?,
    val icon: Icon?,
    val pendingIntent: PendingIntent,
    val actionIntentType: Int,
    val isProgressButton: Boolean = false,
    val progress: Int = 0,
    val progressColor: String? = null,
    val actionBgColor: String? = null,
    val isCCW: Boolean = false,
    val colorReach: String? = null
) {
    /**
     * **Constructor for Standard or Progress Buttons (Type 0 / Type 1)**
     *
     * Creates a button with an Icon loaded from an app resource.
     *
     * @param context App context.
     * @param drawableRes The resource ID of the icon (e.g., `R.drawable.ic_stop`).
     * @param actionIntentType 1=Activity, 2=Broadcast, 3=Service.
     */
    constructor(
        key: String,
        title: CharSequence?,
        context: Context,
        drawableRes: Int,
        pendingIntent: PendingIntent,
        actionIntentType: Int,
        isProgressButton: Boolean = false,
        progress: Int = 0,
        progressColor: String? = null,
        actionBgColor: String? = null,
        isCCW: Boolean = false,
        colorReach: String? = null
    ) : this(
        key = key,
        title = title,
        // Icon.createWithResource is required for cross-process rendering in SystemUI
        icon = Icon.createWithResource(context, drawableRes),
        pendingIntent = pendingIntent,
        actionIntentType = actionIntentType,
        isProgressButton = isProgressButton,
        progress = progress,
        progressColor = progressColor,
        actionBgColor = actionBgColor,
        isCCW = isCCW,
        colorReach = colorReach
    )

    /**
     * **Constructor for Dynamic Image Buttons**
     *
     * Creates a button with an Icon generated from a Bitmap (e.g., downloaded image).
     *
     * @param bitmap The Bitmap to use as the icon.
     */
    constructor(
        key: String,
        title: CharSequence?,
        bitmap: Bitmap,
        pendingIntent: PendingIntent,
        actionIntentType: Int,
        isProgressButton: Boolean = false,
        progress: Int = 0,
        progressColor: String? = null,
        actionBgColor: String? = null,
        isCCW: Boolean = false,
        colorReach: String? = null
    ) : this(
        key = key,
        title = title,
        icon = Icon.createWithBitmap(bitmap),
        pendingIntent = pendingIntent,
        actionIntentType = actionIntentType,
        isProgressButton = isProgressButton,
        progress = progress,
        progressColor = progressColor,
        actionBgColor = actionBgColor,
        isCCW = isCCW,
        colorReach = colorReach
    )

    /**
     * **Constructor for Text-Only Buttons (Type 2)**
     *
     * Creates a button with text and a background color, but NO icon.
     * The system requires a non-null icon object internally, but it will not be displayed.
     *
     * @param title The text to display.
     * @param actionBgColor The background color (e.g., "#E6E6E6" or "#FF3B30").
     */
    constructor(
        key: String,
        title: CharSequence,
        pendingIntent: PendingIntent,
        actionIntentType: Int,
        actionBgColor: String? = null
    ) : this(
        key = key,
        title = title,
        icon = null, // Explicitly null signals Type 2
        pendingIntent = pendingIntent,
        actionIntentType = actionIntentType,
        isProgressButton = false,
        progress = 0,
        progressColor = null,
        actionBgColor = actionBgColor,
        isCCW = false,
        colorReach = null
    )
}

/**
 * Represents an image resource used in the notification (e.g., content image, progress icon).
 *
 * @property key A unique string ID for this picture (e.g., "pic_cover_art").
 * @property icon The [Icon] object.
 */
data class HyperPicture(
    val key: String,
    val icon: Icon
) {
    /**
     * Creates a picture from a drawable resource ID. Preferred for static assets.
     */
    constructor(key: String, context: Context, drawableRes: Int) : this(
        key = key,
        icon = Icon.createWithResource(context, drawableRes)
    )

    /**
     * Creates a picture from a Bitmap. Use for dynamic/downloaded images.
     */
    constructor(key: String, bitmap: Bitmap) : this(
        key = key,
        icon = Icon.createWithBitmap(bitmap)
    )
}

/**
 * Internal helper to create a transparent 1x1 px icon.
 * Used as a placeholder for Text-Only buttons to satisfy Android API requirements without rendering an image.
 */
private fun createTransparentIcon(context: Context): Icon {
    return Icon.createWithResource(context, android.R.drawable.screen_background_light_transparent)
}

/**
 * **The Main Builder for Xiaomi HyperIsland Notifications.**
 *
 * This class constructs the complex JSON parameter string (`miui.focus.param`) and the
 * resource Bundle (`miui.focus.actions`, `miui.focus.pics`) required by HyperOS.
 *
 * **Usage Workflow:**
 * 1. Configure the notification using `set...` methods (e.g., `setChatInfo`, `setBigIslandInfo`).
 * 2. Add any resources using `addAction` and `addPicture`.
 * 3. Call `buildResourceBundle()` and pass it to `NotificationCompat.Builder.addExtras()`.
 * 4. Call `buildJsonParam()` and add it to `notification.extras` via `putString("miui.focus.param", json)`.
 *
 * @param context The application context.
 * @param businessName A unique string ID for your feature (e.g., "timer", "taxi", "music").
 * @param ticker Text shown in the status bar (legacy OS2 behavior).
 */
class HyperIslandNotification private constructor(
    private val context: Context,
    private val businessName: String,
    private val ticker: String
) {
    // Internal state for JSON generation
    private var targetPage: String? = null
    private var chatInfo: ChatInfo? = null
    private var baseInfo: BaseInfo? = null
    private var paramIsland: ParamIsland? = null
    private var progressBar: ProgressInfo? = null
    private var multiProgressInfo: MultiProgressInfo? = null
    private var hintInfo: HintInfo? = null
    private var stepInfo: StepInfo? = null

    // Configuration flags
    private var timeout: Long? = null
    private var enableFloat: Boolean = true
    private var isShownNotification: Boolean = true

    // Resources
    private val actions = mutableListOf<HyperAction>()
    private val pictures = mutableListOf<HyperPicture>()

    // JSON Configuration
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonSerializer = Json {
        encodeDefaults = true // Include default values
        explicitNulls = false // Omit null fields to keep JSON clean
    }

    /**
     * Internal logic to determine the button type based on properties.
     */
    private fun HyperAction.getType(): Int {
        return when {
            this.isProgressButton -> 1 // Circular Progress
            this.icon == null && this.title != null -> 2 // Text Only
            else -> 0 // Standard Icon
        }
    }

    // ============================================================================================
    // CONFIGURATION METHODS
    // ============================================================================================

    /**
     * Sets the auto-hide timeout for the island.
     * @param durationMs Duration in milliseconds. If not set, the system decides (usually persistent).
     */
    fun setTimeout(durationMs: Long) = apply { this.timeout = durationMs }

    /**
     * Controls whether the notification "floats" (pops up) when first posted or updated.
     * Set `false` for silent updates.
     */
    fun setEnableFloat(enable: Boolean) = apply { this.enableFloat = enable }

    /**
     * Controls whether the notification is visible in the system notification shade.
     * If `false`, it may only appear as an Island.
     */
    fun setShowNotification(show: Boolean) = apply { this.isShownNotification = show }

    /**
     * **Required Feature:** Enables the "drag-to-open" gesture.
     * When the user long-presses and drags the island, it opens this Activity in a floating window.
     *
     * @param fullyQualifiedActivityName Full class name (e.g., `com.example.app.MainActivity`).
     */
    fun setSmallWindowTarget(fullyQualifiedActivityName: String) = apply { this.targetPage = fullyQualifiedActivityName }

    /**
     * **Top Hint:** Adds a small capsule notification *above* the main island.
     * Useful for secondary alerts (e.g., "New Message" while on a call).
     *
     * @param title The text to display.
     * @param actionKey Optional key for an action button inside the hint.
     */
    fun setHintInfo(title: String, actionKey: String? = null) = apply {
        val actionRef = actionKey?.let { key ->
            SimpleActionRef(action = ACTION_PREFIX + key)
        }
        this.hintInfo = HintInfo(
            title = title,
            actionInfo = actionRef
        )
    }

    /**
     * **Notification Panel Progress (Step/Node Style).**
     * Displays a segmented progress bar on the notification panel.
     *
     * @param title Text displayed above the bar (e.g., "Step 2/4").
     * @param progress Percentage (0-100).
     * @param color Highlight color (e.g., "#00FF00").
     * @param points Number of segment dividers (0-4). E.g., 3 points = 4 segments.
     */
    fun setMultiProgress(
        title: String,
        progress: Int,
        color: String? = null,
        points: Int = 0
    ) = apply {
        // Xiaomi limits: Progress 0-4 segments (not %), Points 0-4.
        // Wait, documentation actually says progress is int (likely %), but points logic splits it.
        // Let's trust user input but ensure points are safe.
        val safePoints = points.coerceIn(0, 4)

        this.multiProgressInfo = MultiProgressInfo(
            title = title,
            progress = progress,
            color = color,
            points = safePoints
        )
    }

    /**
     * **Notification Panel Progress (Linear Style).**
     * Adds a standard linear progress bar to the bottom of the notification.
     * Supports icons at the start, middle, and end.
     *
     * @param progress Percentage (0-100).
     * @param color Start color of the gradient.
     * @param colorEnd End color of the gradient.
     * @param picForwardKey Key for the icon moving with the progress bar head.
     * @param picMiddleKey Key for the middle icon (when passed).
     * @param picMiddleUnselectedKey Key for the middle icon (not yet passed).
     * @param picEndKey Key for the end icon (when passed).
     * @param picEndUnselectedKey Key for the end icon (not yet passed).
     */
    fun setProgressBar(
        progress: Int,
        color: String? = null,
        colorEnd: String? = null,
        picForwardKey: String? = null,
        picMiddleKey: String? = null,
        picMiddleUnselectedKey: String? = null,
        picEndKey: String? = null,
        picEndUnselectedKey: String? = null
    ) = apply {
        this.progressBar = ProgressInfo(
            progress = progress,
            colorProgress = color,
            colorProgressEnd = colorEnd,
            // Automatically apply picture prefix
            picForward = picForwardKey?.let { it },
            picMiddle = picMiddleKey?.let { it },
            picMiddleUnselected = picMiddleUnselectedKey?.let { it },
            picEnd = picEndKey?.let { it },
            picEndUnselected = picEndUnselectedKey?.let { it }
        )
    }

    /**
     * **Legacy Step Progress:** For displaying step info on the Island itself (less common).
     */
    fun setStepProgress(currentStep: Int, totalStep: Int, activeColor: String? = null) = apply {
        this.stepInfo = StepInfo(
            currentStep = currentStep,
            totalStep = totalStep,
            activeColor = activeColor
        )
    }

    // ============================================================================================
    // PANEL TEMPLATES
    // ============================================================================================

    /**
     * Sets the "Chat" style notification panel.
     * Suitable for messaging, timers, or status updates.
     *
     * @param actionKeys List of action keys to display as buttons.
     */
    fun setChatInfo(
        title: String,
        content: String? = null,
        pictureKey: String? = null,
        timer: TimerInfo? = null,
        actionKeys: List<String>? = null
    ) = apply {
        // Convert keys to Action References for the JSON
        val actionRefs = actionKeys?.mapNotNull { key ->
            actions.firstOrNull { it.key == key }?.let { action ->
                action.toActionRef(isFullDefinition = false) // False = Reference Mode
            }
        }?.ifEmpty { null }

        this.chatInfo = ChatInfo(
            title = title,
            content = if (timer != null) null else content,
            picFunction = pictureKey?.let { PIC_PREFIX + it },
            timerInfo = timer,
            actions = actionRefs
        )
        this.baseInfo = null
    }

    /**
     * Sets the "Base" style notification panel.
     * Suitable for general info.
     *
     * @param type Layout template (1 or 2). Type 2 often supports richer content.
     * @param titleColor Custom hex color for the title.
     */
    fun setBaseInfo(
        title: String,
        content: String,
        subTitle: String? = null,
        pictureKey: String? = null,
        type: Int = 1,
        titleColor: String? = null,
        actionKeys: List<String>? = null
    ) = apply {
        val actionRefs = actionKeys?.mapNotNull { key ->
            actions.firstOrNull { it.key == key }?.let { action ->
                action.toActionRef(isFullDefinition = false)
            }
        }?.ifEmpty { null }

        this.baseInfo = BaseInfo(
            type = type,
            title = title,
            subTitle = subTitle,
            content = content,
            picFunction = pictureKey?.let { PIC_PREFIX + it },
            colorTitle = titleColor,
            actions = actionRefs
        )
        this.chatInfo = null
    }

    // ============================================================================================
    // ISLAND CONFIGURATION
    // ============================================================================================

    private fun prefixPicInfo(picInfo: PicInfo?): PicInfo? {
        return picInfo?.copy(pic = PIC_PREFIX + picInfo.pic)
    }

    /**
     * Sets the **Summary Island** (Small Pill) content.
     * Supports split content (Left/Right).
     */
    fun setSmallIsland(aZone: ImageTextInfoLeft, bZone: ImageTextInfoRight?) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val fixedA = aZone.copy(picInfo = prefixPicInfo(aZone.picInfo))
        val fixedB = bZone?.copy(picInfo = prefixPicInfo(bZone.picInfo))
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(imageTextInfoLeft = fixedA, imageTextInfoRight = fixedB))
    }

    /**
     * Sets the **Summary Island** to a single icon.
     */
    fun setSmallIslandIcon(picKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(picInfo = PicInfo(type = 1, pic = PIC_PREFIX + picKey)))
    }

    /**
     * Sets the **Summary Island** to an icon + circular progress ring.
     */
    fun setSmallIslandCircularProgress(pictureKey: String, progress: Int, color: String? = null, isCCW: Boolean = false) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val progressComponent = CombinePicInfo(picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), progressInfo = CircularProgressInfo(progress = progress, colorReach = color, isCCW = isCCW))
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(combinePicInfo = progressComponent))
    }

    /**
     * Sets the **Expanded Island** (Big Island) content.
     * Supports split content (Left/Right) and action buttons.
     */
    fun setBigIslandInfo(
        left: ImageTextInfoLeft? = null,
        right: ImageTextInfoRight? = null,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

        // BigIsland uses SimpleActionRef (Just the keys {"action": "..."})
        val actionRefs = actionKeys?.map { key ->
            SimpleActionRef(action = ACTION_PREFIX + key)
        }?.ifEmpty { null }

        val fixedLeft = left?.copy(picInfo = prefixPicInfo(left.picInfo))
        val fixedRight = right?.copy(picInfo = prefixPicInfo(right.picInfo))

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = fixedLeft,
                imageTextInfoRight = fixedRight,
                sameWidthDigitInfo = null,
                actions = actionRefs
            )
        )
    }

    fun setBigIslandInfo(info: ImageTextInfoLeft) = setBigIslandInfo(left = info)

    /**
     * Sets the **Expanded Island** to an icon + circular progress layout.
     */
    fun setBigIslandProgressCircle(
        pictureKey: String,
        title: String,
        progress: Int,
        color: String? = null,
        isCCW: Boolean = false,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), textInfo = TextInfo(title = title, content = null))
        val progressComponent = ProgressTextInfo(progressInfo = CircularProgressInfo(progress = progress, colorReach = color, isCCW = isCCW), textInfo = null)

        val actionRefs = actionKeys?.map { key ->
            SimpleActionRef(action = ACTION_PREFIX + key)
        }?.ifEmpty { null }

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                progressTextInfo = progressComponent,
                actions = actionRefs
            )
        )
    }

    /**
     * Sets the **Expanded Island** to a Countdown Timer.
     */
    fun setBigIslandCountdown(
        countdownTime: Long,
        pictureKey: String,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), textInfo = null)

        val actionRefs = actionKeys?.map { key ->
            SimpleActionRef(action = ACTION_PREFIX + key)
        }?.ifEmpty { null }

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo = timerInfo),
                actions = actionRefs
            )
        )
    }

    /**
     * Sets the **Expanded Island** to a Count-Up Timer.
     */
    fun setBigIslandCountUp(
        startTime: Long,
        pictureKey: String,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = PIC_PREFIX + pictureKey), textInfo = null)

        val actionRefs = actionKeys?.map { key ->
            SimpleActionRef(action = ACTION_PREFIX + key)
        }?.ifEmpty { null }

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = leftInfo,
                sameWidthDigitInfo = SameWidthDigitInfo(timerInfo = timerInfo),
                actions = actionRefs
            )
        )
    }

    // --- Resource Registration ---
    fun addAction(action: HyperAction) = apply { this.actions.add(action) }
    fun addPicture(picture: HyperPicture) = apply { this.pictures.add(picture) }

    // ============================================================================================
    // BUILD METHODS
    // ============================================================================================

    /**
     * **Step 1:** Builds the Bundle containing `PendingIntent`s and `Icon`s.
     * Pass this to `.addExtras()`.
     */
    fun buildResourceBundle(): Bundle {
        val bundle = Bundle()
        if (!isSupported(context)) return bundle

        val actionsBundle = Bundle()
        actions.forEach {
            // If icon is null, use transparent placeholder to prevent crashes
            val actionIcon = it.icon ?: createTransparentIcon(context)
            val notificationAction = Notification.Action.Builder(actionIcon, it.title, it.pendingIntent).build()
            // Key must be prefixed in Bundle
            actionsBundle.putParcelable(ACTION_PREFIX + it.key, notificationAction)
        }
        bundle.putBundle("miui.focus.actions", actionsBundle)

        val picsBundle = Bundle()
        pictures.forEach {
            // Key must be prefixed in Bundle
            picsBundle.putParcelable(PIC_PREFIX + it.key, it.icon)
        }
        bundle.putBundle("miui.focus.pics", picsBundle)

        return bundle
    }

    /**
     * **Step 2:** Builds the JSON parameter string.
     * Pass this to `notification.extras.putString("miui.focus.param", json)`.
     */
    fun buildJsonParam(): String {
        val paramV2 = ParamV2(
            business = businessName,
            ticker = ticker,
            smallWindowInfo = targetPage?.let { SmallWindowInfo(it) },
            chatInfo = this.chatInfo,
            baseInfo = this.baseInfo,
            paramIsland = this.paramIsland,
            // Top-level Dictionary: Full definitions
            actions = this.actions.map { it.toActionRef(true) }.ifEmpty { null },
            progressInfo = this.progressBar,
            multiProgressInfo = this.multiProgressInfo,
            hintInfo = this.hintInfo,
            stepInfo = this.stepInfo,
            timeout = this.timeout,
            enableFloat = this.enableFloat,
            isShownNotification = this.isShownNotification,
            islandFirstFloat = this.enableFloat
        )
        val payload = HyperIslandPayload(paramV2)
        val jsonString = jsonSerializer.encodeToString(payload)
        Log.d("HyperIsland", "Payload JSON: $jsonString")
        return jsonString
    }

    /**
     * Converts a HyperAction to its JSON representation.
     *
     * @param isFullDefinition
     * - **true (Dictionary):** `action = KEY`, `actionIntent = NULL`. This defines the action.
     * - **false (Reference):** `action = NULL`, `actionIntent = KEY`. This links to the definition.
     */
    private fun HyperAction.toActionRef(isFullDefinition: Boolean): HyperActionRef {
        val prefixedKey = ACTION_PREFIX + this.key

        if (isFullDefinition) {
            // Dictionary: Full Definition
            return HyperActionRef(
                type = this.getType(),
                action = prefixedKey,
                actionIntent = null,
                actionIntentType = this.actionIntentType,
                progressInfo = if (this.isProgressButton) ProgressInfo(
                    progress = this.progress,
                    colorProgress = this.colorReach ?: this.progressColor,
                ) else null,
                actionTitle = this.title?.toString(),
                actionBgColor = this.actionBgColor
            )
        } else {
            // Reference: Link + Visuals
            return HyperActionRef(
                type = this.getType(),
                action = null,
                actionIntent = prefixedKey,
                actionIntentType = this.actionIntentType,
                progressInfo = if (this.isProgressButton) ProgressInfo(
                    progress = this.progress,
                    colorProgress = this.colorReach ?: this.progressColor,
                ) else null,
                actionTitle = this.title?.toString(),
                actionBgColor = this.actionBgColor
            )
        }
    }

    companion object {
        fun Builder(context: Context, businessName: String, ticker: String): HyperIslandNotification {
            return HyperIslandNotification(context, businessName, ticker)
        }
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