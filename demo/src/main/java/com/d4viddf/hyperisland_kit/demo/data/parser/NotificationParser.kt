package com.d4viddf.hyperisland_kit.demo.data.parser

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.graphics.createBitmap
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedAction
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedNotification
import com.d4viddf.hyperisland_kit.demo.data.model.ResourceMeta
import kotlinx.serialization.json.Json
import java.util.UUID

object NotificationParser {
    private const val TAG = "NotificationParser"
    private val jsonPretty = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun parse(context: Context, sbn: StatusBarNotification): Pair<InspectedNotification, Map<String, Bitmap>> {
        val notif = sbn.notification
        val extras = notif.extras

        val appName = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val styleClass = extras.getString(Notification.EXTRA_TEMPLATE)
        val styleSimpleName = styleClass?.substringAfterLast(".")

        val imagesMap = mutableMapOf<String, Bitmap>()
        val metaMap = mutableMapOf<String, ResourceMeta>()

        fun processIcon(key: String, icon: Icon?) {
            if (icon == null) return
            val meta = extractIconMeta(icon)
            metaMap[key] = meta

            iconToBitmap(context, icon)?.let {
                imagesMap[key] = it
                metaMap[key] = meta.copy(width = it.width, height = it.height, fileSize = "${it.byteCount / 1024} KB")
            }
        }

        processIcon("Small Icon", notif.smallIcon)
        notif.getLargeIcon()?.let { processIcon("Large Icon", it) }

        @Suppress("DEPRECATION")
        val picBackground = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        if (picBackground != null) {
            imagesMap["Style Big Picture"] = picBackground
            metaMap["Style Big Picture"] = ResourceMeta("BITMAP", "Raw Parcelable", picBackground.width, picBackground.height, "${picBackground.byteCount / 1024} KB")
        }

        val picsBundle = extras.getBundle("miui.focus.pics")
        if (picsBundle != null) {
            for (key in picsBundle.keySet()) {
                val icon = getIconFromBundle(picsBundle, key)
                processIcon("Hyper: ${key.removePrefix("miui.focus.pic_")}", icon)
            }
        }

        val actionsList = notif.actions?.mapIndexed { index, action ->
            val iconKey = "Action $index Icon"
            processIcon(iconKey, action.getIcon())

            InspectedAction(
                title = action.title?.toString() ?: "Unnamed",
                iconKey = if (action.getIcon() != null) iconKey else null,
                intentDescription = describePendingIntent(action.actionIntent)
            )
        } ?: emptyList()

        val styleInfo = mutableMapOf<String, String>()
        if (styleSimpleName == "MediaStyle" || styleSimpleName == "DecoratedMediaCustomViewStyle") {
            @Suppress("DEPRECATION")
            val token = extras.getParcelable<Parcelable>(Notification.EXTRA_MEDIA_SESSION)
            styleInfo["Media Token"] = token?.toString() ?: "None"
        }
        extras.keySet().forEach { key ->
            @Suppress("DEPRECATION")
            val value = extras.get(key)
            if (value !is Bitmap && value !is Icon && value !is Bundle) {
                styleInfo[key] = value?.toString()?.take(100) ?: "null"
            }
        }

        // Check all Xiaomi HyperOS Focus / HyperIsland payload keys
        val rawHyperPayload = extras.getString("miui.focus.param")
            ?: extras.getString("miui.focus.param.media")
            ?: extras.getString("miui.focus.param.custom")
            ?: if (picsBundle != null || extras.containsKey("miui.focus.rv")) {
                "{\"type\": \"HyperIsland Focus Event\", \"hasPics\": ${picsBundle != null}, \"hasCustomView\": ${extras.containsKey("miui.focus.rv")}}"
            } else null

        val inspected = InspectedNotification(
            key = "${sbn.packageName}_${sbn.id}_${sbn.postTime}_${UUID.randomUUID().toString().take(4)}",
            id = sbn.id,
            packageName = sbn.packageName,
            appName = appName,
            postTime = sbn.postTime,
            title = title,
            content = content,
            templateStyle = styleSimpleName,
            isOngoing = sbn.isOngoing,
            contentIntent = describePendingIntent(notif.contentIntent),
            actions = actionsList,
            styleExtras = styleInfo,
            hyperJson = tryFormatJson(rawHyperPayload),
            imagePaths = emptyMap(),
            resourceMeta = metaMap
        )

        Log.d(TAG, "Parsed notification: ${sbn.packageName} id=${sbn.id} title='$title' isHyper=${rawHyperPayload != null}")
        return Pair(inspected, imagesMap)
    }

    private fun extractIconMeta(icon: Icon): ResourceMeta {
        val typeStr = when (icon.type) {
            Icon.TYPE_BITMAP -> "BITMAP"
            Icon.TYPE_RESOURCE -> "RESOURCE"
            Icon.TYPE_DATA -> "DATA"
            Icon.TYPE_URI -> "URI"
            Icon.TYPE_ADAPTIVE_BITMAP -> "ADAPTIVE_BITMAP"
            else -> "UNKNOWN (${icon.type})"
        }

        val sourceStr = if (icon.type == Icon.TYPE_RESOURCE) {
            try {
                "${icon.resPackage} (ID: ${icon.resId})"
            } catch (e: Exception) {
                "Res ID: ${icon.resId}"
            }
        } else if (icon.type == Icon.TYPE_URI) {
            icon.uri.toString()
        } else {
            "Memory"
        }

        return ResourceMeta(typeStr, sourceStr, 0, 0, "Unknown")
    }

    private fun describePendingIntent(pi: PendingIntent?): String = pi?.toString() ?: "None"

    private fun tryFormatJson(jsonStr: String?): String? {
        if (jsonStr == null) return null
        return try {
            val element = jsonPretty.parseToJsonElement(jsonStr)
            jsonPretty.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), element)
        } catch (e: Exception) {
            jsonStr
        }
    }

    private fun getIconFromBundle(bundle: Bundle, key: String): Icon? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, Icon::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key) as? Icon
        }
    }

    private fun iconToBitmap(context: Context, icon: Icon): Bitmap? {
        return try {
            val drawable = icon.loadDrawable(context) ?: return null
            if (drawable is BitmapDrawable) return drawable.bitmap
            if (drawable.intrinsicWidth <= 0) return null
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
