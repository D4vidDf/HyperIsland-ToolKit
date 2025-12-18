package com.d4viddf.hyperisland_kit.demo

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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import androidx.core.graphics.createBitmap

object NotificationParser {
    private val jsonPretty = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun parse(context: Context, sbn: StatusBarNotification): Pair<InspectedNotification, Map<String, Bitmap>> {
        val notif = sbn.notification
        val extras = notif.extras

        // 1. Basic Info
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val styleClass = extras.getString(Notification.EXTRA_TEMPLATE)
        val styleSimpleName = styleClass?.substringAfterLast(".")

        // 2. Images & Metadata Collections
        val imagesMap = mutableMapOf<String, Bitmap>()
        val metaMap = mutableMapOf<String, ResourceMeta>()

        // Helper to process any icon found
        fun processIcon(key: String, icon: Icon?) {
            if (icon == null) return

            // A. Extract Metadata
            val meta = extractIconMeta(context, icon)
            metaMap[key] = meta

            // B. Extract Bitmap
            iconToBitmap(context, icon)?.let {
                imagesMap[key] = it
                // Update meta with actual bitmap dimensions if available
                metaMap[key] = meta.copy(width = it.width, height = it.height, fileSize = "${it.byteCount / 1024} KB")
            }
        }

        // 3. Extract Standard Icons
        processIcon("Small Icon", notif.smallIcon)
        notif.getLargeIcon()?.let { processIcon("Large Icon", it) }

        // Extract Media/Style Images
        @Suppress("DEPRECATION")
        val picBackground = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        if (picBackground != null) {
            imagesMap["Style Big Picture"] = picBackground
            metaMap["Style Big Picture"] = ResourceMeta("BITMAP", "Raw Parcelable", picBackground.width, picBackground.height, "${picBackground.byteCount/1024} KB")
        }

        // 4. Extract HyperIsland Images
        val picsBundle = extras.getBundle("miui.focus.pics")
        if (picsBundle != null) {
            for (key in picsBundle.keySet()) {
                val icon = getIconFromBundle(picsBundle, key)
                processIcon("Hyper: ${key.removePrefix("miui.focus.pic_")}", icon)
            }
        }

        // 5. Actions (Buttons)
        val actionsList = notif.actions?.mapIndexed { index, action ->
            val iconKey = "Action $index Icon"
            processIcon(iconKey, action.getIcon())

            InspectedAction(
                title = action.title?.toString() ?: "Unnamed",
                iconKey = if (action.getIcon() != null) iconKey else null,
                intentDescription = describePendingIntent(action.actionIntent)
            )
        } ?: emptyList()

        // 6. Style Extras
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

        val inspected = InspectedNotification(
            key = "${sbn.packageName}_${sbn.postTime}_${UUID.randomUUID().toString().take(4)}",
            id = sbn.id,
            packageName = sbn.packageName,
            postTime = sbn.postTime,
            title = title,
            content = content,
            templateStyle = styleSimpleName,
            isOngoing = sbn.isOngoing,
            contentIntent = describePendingIntent(notif.contentIntent),
            actions = actionsList,
            styleExtras = styleInfo,
            hyperJson = tryFormatJson(extras.getString("miui.focus.param")),
            imagePaths = emptyMap(),
            resourceMeta = metaMap // [NEW] Attach metadata
        )

        return Pair(inspected, imagesMap)
    }

    private fun extractIconMeta(context: Context, icon: Icon): ResourceMeta {
        val typeStr = when(icon.type) {
            Icon.TYPE_BITMAP -> "BITMAP"
            Icon.TYPE_RESOURCE -> "RESOURCE"
            Icon.TYPE_DATA -> "DATA"
            Icon.TYPE_URI -> "URI"
            Icon.TYPE_ADAPTIVE_BITMAP -> "ADAPTIVE_BITMAP"
            else -> "UNKNOWN (${icon.type})"
        }

        var sourceStr = "Unknown"

        if (icon.type == Icon.TYPE_RESOURCE) {
            // Try to resolve resource name (e.g., com.example:drawable/ic_icon)
            sourceStr = try {
                val pkg = icon.resPackage
                val id = icon.resId
                // We can't easily get the name without the other app's resources,
                // but we can show the package and ID
                "$pkg (ID: $id)"
            } catch (e: Exception) { "Res ID: ${icon.resId}" }
        } else if (icon.type == Icon.TYPE_URI) {
            sourceStr = icon.uri.toString()
        }

        return ResourceMeta(typeStr, sourceStr, 0, 0, "Unknown")
    }

    private fun describePendingIntent(pi: PendingIntent?): String {
        return pi?.toString() ?: "None"
    }

    private fun tryFormatJson(json: String?): String? {
        if (json == null) return null
        return try {
            val element = jsonPretty.parseToJsonElement(json)
            jsonPretty.encodeToString(JsonObject.serializer(), element as JsonObject)
        } catch (e: Exception) { json }
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
        } catch (e: Exception) { null }
    }
}