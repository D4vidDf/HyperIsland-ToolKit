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
 * The prefix required by the Xiaomi framework for all action keys.
 */
private const val ACTION_PREFIX = "miui.focus.action_"

/**
 * Represents a single clickable action in a HyperIsland notification.
 *
 * @property key A unique string ID for this action (e.g., "action_close").
 * @property title The text to display on the button. Set to `null` for progress-only buttons.
 * @property icon The [Icon] to display on the button. Can be `null` for text-only buttons.
 * @property pendingIntent The [PendingIntent] to fire when the action is clicked.
 * @property actionIntentType The type of intent: 1 for Activity, 2 for Broadcast, 3 for Service.
 * @property isProgressButton Set to `true` if this action should be rendered as a circular progress button.
 * @property progress The progress value (0-100) if [isProgressButton] is `true`.
 * @property progressColor The hex color string (e.g., "#FF8514") for the progress bar.
 * @property actionBgColor The hex color string (e.g., "#FF3B30") for the button's background.
 */
data class HyperAction(
    val key: String,
    val title: CharSequence?,
    val icon: Icon?, // Null for text-only buttons
    val pendingIntent: PendingIntent,
    val actionIntentType: Int, // 1=Activity, 2=Broadcast
    val isProgressButton: Boolean = false,
    val progress: Int = 0,
    val progressColor: String? = null,
    val actionBgColor: String? = null,
    val isCCW: Boolean = false,
    val colorReach: String? = null
) {
    /**
     * Secondary constructor for an ICON + TEXT button
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
     * Secondary constructor for a BUTTON with BITMAP ICON.
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
     * Secondary constructor for a TEXT-ONLY button.
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
        icon = null, // Icon is explicitly null
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
 * Represents a single image or icon resource for a HyperIsland notification.
 */
data class HyperPicture(
    val key: String,
    val icon: Icon
) {
    /**
     * Secondary constructor to create a [HyperPicture] from a drawable resource ID.
     */
    constructor(key: String, context: Context, drawableRes: Int) : this(
        key = key,
        icon = Icon.createWithResource(context, drawableRes)
    )

    /**
     * Secondary constructor to create a [HyperPicture] from a [Bitmap].
     */
    constructor(key: String, bitmap: Bitmap) : this(
        key = key,
        icon = Icon.createWithBitmap(bitmap)
    )
}

/**
 * Creates a placeholder icon for text-only buttons.
 */
private fun createTransparentIcon(context: Context): Icon {
    return Icon.createWithResource(context, android.R.drawable.screen_background_light_transparent)
}

/**
 * Main builder class for creating Xiaomi HyperIsland notifications.
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
    private var multiProgressInfo: MultiProgressInfo? = null

    // --- Configurations ---
    private var hintInfo: HintInfo? = null
    private var stepInfo: StepInfo? = null
    private var timeout: Long? = null
    private var enableFloat: Boolean = true
    private var isShownNotification: Boolean = true
    // --- NEW: Log Configuration ---
    private var logEnabled: Boolean = true

    private val actions = mutableListOf<HyperAction>()
    private val pictures = mutableListOf<HyperPicture>()

    @OptIn(ExperimentalSerializationApi::class)
    private val jsonSerializer = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    private fun HyperAction.getType(): Int {
        return when {
            this.isProgressButton -> 1
            this.icon == null && this.title != null -> 2
            else -> 0
        }
    }

    // --- Public Builder Methods ---

    /**
     * Controls whether the library outputs debug logs (e.g. the generated JSON payload).
     * Default is true.
     */
    fun setLogEnabled(enabled: Boolean) = apply { this.logEnabled = enabled }

    fun setTimeout(durationMs: Long) = apply { this.timeout = durationMs }
    fun setEnableFloat(enable: Boolean) = apply { this.enableFloat = enable }
    fun setShowNotification(show: Boolean) = apply { this.isShownNotification = show }
    fun setSmallWindowTarget(fullyQualifiedActivityName: String) = apply { this.targetPage = fullyQualifiedActivityName }

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
     * Sets the Multi-Progress (Step/Node) Info.
     * Limits applied:
     * - Progress: 0 to 4
     * - Points: 0 to 4
     */
    fun setMultiProgress(
        title: String,
        progress: Int,
        color: String? = null,
        points: Int = 0
    ) = apply {
        val safeProgress = progress.coerceIn(0, 4)
        val safePoints = points.coerceIn(0, 4)

        this.multiProgressInfo = MultiProgressInfo(
            title = title,
            progress = safeProgress,
            color = color,
            points = safePoints
        )
    }

    fun setStepProgress(currentStep: Int, totalStep: Int, activeColor: String? = null) = apply {
        this.stepInfo = StepInfo(
            currentStep = currentStep,
            totalStep = totalStep,
            activeColor = activeColor
        )
    }

    fun setChatInfo(
        title: String,
        content: String? = null,
        pictureKey: String? = null,
        timer: TimerInfo? = null,
        actionKeys: List<String>? = null
    ) = apply {
        val actionRefs = actionKeys?.mapNotNull { key ->
            actions.firstOrNull { it.key == key }?.let { action ->
                action.toActionRef(isFullDefinition = false)
            }
        }?.ifEmpty { null }

        this.chatInfo = ChatInfo(
            title = title,
            content = if (timer != null) null else content,
            picFunction = pictureKey,
            timerInfo = timer,
            actions = actionRefs
        )
        this.baseInfo = null
    }

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
            picFunction = pictureKey,
            colorTitle = titleColor,
            actions = actionRefs
        )
        this.chatInfo = null
    }

    private fun prefixPicInfo(picInfo: PicInfo?): PicInfo? {
        // PicInfo is tricky because the key is inside the object, not a map key.
        // Assuming the user passed the raw key, we should prefix it if it looks like a key.
        // However, PicInfo structure in this library uses a 'pic' string.
        // If 'pic' is a resource key, it needs prefix "miui.focus.pic_".
        // In this implementation, we assume all pics passed to Island components are keys.
        return picInfo?.copy(pic = "miui.focus.pic_" + picInfo.pic)
    }

    fun setSmallIsland(aZone: ImageTextInfoLeft, bZone: ImageTextInfoRight?) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        // Apply prefix to the keys inside the island objects
        val fixedA = aZone.copy(picInfo = prefixPicInfo(aZone.picInfo))
        val fixedB = bZone?.copy(picInfo = prefixPicInfo(bZone.picInfo))
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(imageTextInfoLeft = fixedA, imageTextInfoRight = fixedB))
    }

    fun setSmallIslandIcon(picKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(picInfo = PicInfo(type = 1, pic = "miui.focus.pic_" + picKey)))
    }

    fun setSmallIslandCircularProgress(pictureKey: String, progress: Int, color: String? = null, isCCW: Boolean = false) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val progressComponent = CombinePicInfo(picInfo = PicInfo(type = 1, pic = "miui.focus.pic_" + pictureKey), progressInfo = CircularProgressInfo(progress = progress, colorReach = color, isCCW = isCCW))
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(combinePicInfo = progressComponent))
    }

    fun setBigIslandInfo(
        left: ImageTextInfoLeft? = null,
        right: ImageTextInfoRight? = null,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

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

    fun setBigIslandProgressCircle(
        pictureKey: String,
        title: String,
        progress: Int,
        color: String? = null,
        isCCW: Boolean = false,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()

        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_" + pictureKey), textInfo = TextInfo(title = title, content = null))
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

    fun setBigIslandCountdown(
        countdownTime: Long,
        pictureKey: String,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(-1, countdownTime, System.currentTimeMillis(), System.currentTimeMillis())
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_" + pictureKey), textInfo = null)

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

    fun setBigIslandCountUp(
        startTime: Long,
        pictureKey: String,
        actionKeys: List<String>? = null
    ) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val timerInfo = TimerInfo(1, startTime, startTime, System.currentTimeMillis())
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = "miui.focus.pic_" + pictureKey), textInfo = null)

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
            // Apply prefixes to picture keys
            picForward = picForwardKey?.let { "miui.focus.pic_" + it },
            picMiddle = picMiddleKey?.let { "miui.focus.pic_" + it },
            picMiddleUnselected = picMiddleUnselectedKey?.let { "miui.focus.pic_" + it },
            picEnd = picEndKey?.let { "miui.focus.pic_" + it },
            picEndUnselected = picEndUnselectedKey?.let { "miui.focus.pic_" + it }
        )
    }

    fun addAction(action: HyperAction) = apply {
        this.actions.add(action)
    }

    fun addPicture(picture: HyperPicture) = apply {
        this.pictures.add(picture)
    }

    fun buildResourceBundle(): Bundle {
        val bundle = Bundle()
        if (!isSupported(context)) return bundle
        val actionsBundle = Bundle()
        actions.forEach {
            val actionIcon = it.icon ?: createTransparentIcon(context)
            val notificationAction = Notification.Action.Builder(actionIcon, it.title, it.pendingIntent).build()
            actionsBundle.putParcelable(ACTION_PREFIX + it.key, notificationAction)
        }
        bundle.putBundle("miui.focus.actions", actionsBundle)
        val picsBundle = Bundle()
        pictures.forEach {
            // Prefix Picture Keys in Bundle
            picsBundle.putParcelable("miui.focus.pic_" + it.key, it.icon)
        }
        bundle.putBundle("miui.focus.pics", picsBundle)
        return bundle
    }

    fun buildJsonParam(): String {
        val paramV2 = ParamV2(
            business = businessName,
            ticker = ticker,
            smallWindowInfo = targetPage?.let { SmallWindowInfo(it) },
            chatInfo = this.chatInfo,
            baseInfo = this.baseInfo,
            paramIsland = this.paramIsland,
            // Dictionaries
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
        if (logEnabled) {
            Log.d("HyperIsland", "Payload JSON: $jsonString")
        }
        return jsonString
    }

    private fun HyperAction.toActionRef(isFullDefinition: Boolean): HyperActionRef {
        val prefixedKey = ACTION_PREFIX + this.key

        if (isFullDefinition) {
            return HyperActionRef(
                type = this.getType(),
                action = prefixedKey,
                actionIntent = null,
                actionIntentType = this.actionIntentType,
                progressInfo = if (this.isProgressButton) ProgressInfo(
                    progress = this.progress,
                    colorProgress = this.colorReach ?: this.progressColor
                ) else null,
                actionTitle = this.title?.toString(),
                actionBgColor = this.actionBgColor
            )
        } else {
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