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

private const val ACTION_PREFIX = "miui.focus.action_"

data class HyperAction(
    val key: String,
    val title: CharSequence?,
    val icon: Icon?,
    val pendingIntent: PendingIntent,
    val actionIntentType: Int, // 1=Activity, 2=Broadcast
    val isProgressButton: Boolean = false,
    val progress: Int = 0,
    val progressColor: String? = null,
    val actionBgColor: String? = null,
    val isCCW: Boolean = false,
    val colorReach: String? = null
) {
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

    constructor(
        key: String,
        title: CharSequence,
        pendingIntent: PendingIntent,
        actionIntentType: Int,
        actionBgColor: String? = null
    ) : this(
        key = key,
        title = title,
        icon = null,
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

data class HyperPicture(
    val key: String,
    val icon: Icon
) {
    constructor(key: String, context: Context, drawableRes: Int) : this(
        key = key,
        icon = Icon.createWithResource(context, drawableRes)
    )

    constructor(key: String, bitmap: Bitmap) : this(
        key = key,
        icon = Icon.createWithBitmap(bitmap)
    )
}

private fun createTransparentIcon(context: Context): Icon {
    return Icon.createWithResource(context, android.R.drawable.screen_background_light_transparent)
}

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

    // --- NEW CONFIGURATION VARIABLES ---
    private var timeout: Long? = null
    private var enableFloat: Boolean = true
    private var isShownNotification: Boolean = true

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
     * Sets the timeout for the island notification.
     * @param durationMs The duration in milliseconds before the island hides.
     */
    fun setTimeout(durationMs: Long) = apply {
        this.timeout = durationMs
    }

    /**
     * Controls whether the notification should "float" (pop up) as a heads-up island.
     * Set to false if you are updating an existing notification and don't want it to pop again.
     */
    fun setEnableFloat(enable: Boolean) = apply {
        this.enableFloat = enable
    }

    /**
     * Controls whether the notification is shown in the system notification shade.
     */
    fun setShowNotification(show: Boolean) = apply {
        this.isShownNotification = show
    }

    fun setSmallWindowTarget(fullyQualifiedActivityName: String) = apply {
        this.targetPage = fullyQualifiedActivityName
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
            actions = actionRefs
        )
        this.chatInfo = null
    }

    fun setSmallIsland(aZone: ImageTextInfoLeft, bZone: ImageTextInfoRight?) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(imageTextInfoLeft = aZone, imageTextInfoRight = bZone))
    }

    fun setSmallIslandIcon(picKey: String) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        this.paramIsland = this.paramIsland?.copy(islandProperty = 1, smallIslandArea = SmallIslandArea(picInfo = PicInfo(type = 1, pic = picKey)))
    }

    fun setSmallIslandCircularProgress(pictureKey: String, progress: Int, color: String? = null, isCCW: Boolean = false) = apply {
        if (this.paramIsland == null) this.paramIsland = ParamIsland()
        val progressComponent = CombinePicInfo(picInfo = PicInfo(type = 1, pic = pictureKey), progressInfo = CircularProgressInfo(progress = progress, colorReach = color, isCCW = isCCW))
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

        this.paramIsland = this.paramIsland?.copy(
            islandProperty = 1,
            bigIslandArea = BigIslandArea(
                imageTextInfoLeft = left,
                imageTextInfoRight = right,
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

        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = pictureKey), textInfo = TextInfo(title = title, content = null))
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
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = pictureKey), textInfo = null)

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
        val leftInfo = ImageTextInfoLeft(type = 1, picInfo = PicInfo(type = 1, pic = pictureKey), textInfo = null)

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

    fun setProgressBar(progress: Int, color: String? = null) = apply {
        this.progressBar = ProgressInfo(progress, color)
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
            picsBundle.putParcelable(it.key, it.icon)
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
            progressInfo = this.progressBar,
            // --- NEW PARAMETERS MAPPED HERE ---
            timeout = this.timeout,
            enableFloat = this.enableFloat,
            isShownNotification = this.isShownNotification,
            islandFirstFloat = this.enableFloat // Mapping enableFloat to islandFirstFloat as well for safety
        )
        val payload = HyperIslandPayload(paramV2)
        val jsonString = jsonSerializer.encodeToString(payload)
        Log.d("HyperIsland", "Payload JSON: $jsonString")
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
                    colorProgress = this.colorReach ?: this.progressColor,
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