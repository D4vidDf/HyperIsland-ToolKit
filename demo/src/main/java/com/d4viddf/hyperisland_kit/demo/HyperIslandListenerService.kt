package com.d4viddf.hyperisland_kit.demo

import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class HyperIslandListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras

        // --- STRICT FILTER ---
        // Only process notifications that actually have the HyperIsland JSON data.
        val jsonParam = extras.getString("miui.focus.param")

        if (jsonParam == null) {
            return // Ignore standard notifications
        }

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
        val packageName = sbn.packageName

        // --- DEEP INSPECTION ---
        val detailsMap = mutableMapOf<String, String>()

        // 1. Basic Info
        detailsMap["[BASIC]"] = "--- NOTIFICATION INFO ---"
        detailsMap["ID"] = sbn.id.toString()
        detailsMap["Key"] = sbn.key
        detailsMap["Tag"] = sbn.tag ?: "null"
        detailsMap["Ongoing"] = sbn.isOngoing.toString()
        detailsMap["Clearable"] = sbn.isClearable.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                detailsMap["OpPkg"] = sbn.opPkg
                detailsMap["Uid"] = sbn.uid.toString()
            } catch (e: Exception) { /* Ignore */ }
        }

        // 2. Flags
        val flags = sbn.notification.flags
        detailsMap["Flags (Int)"] = flags.toString()
        val flagNames = getFlagNames(flags)
        if (flagNames.isNotEmpty()) detailsMap["Flags (Decoded)"] = flagNames

        // 3. Recursive Extra Dump
        detailsMap["[EXTRAS]"] = "--- RECURSIVE DUMP ---"
        try {
            deepDump(extras, "", detailsMap)
        } catch (e: Exception) {
            detailsMap["Error"] = "Failed to dump extras: ${e.message}"
        }

        Log.d("HyperInspector", "Captured Island Notification: $title ($packageName)")

        NotificationLogRepository.addLog(
            NotificationLog(
                id = sbn.key,
                timestamp = System.currentTimeMillis(),
                packageName = packageName,
                title = title,
                jsonParam = jsonParam,
                extrasDetails = detailsMap
            )
        )
    }

    /**
     * Recursively digs into Bundles, Arrays, and Lists to find hidden values.
     */
    private fun deepDump(obj: Any?, prefix: String, map: MutableMap<String, String>) {
        if (obj == null) {
            map[prefix] = "null"
            return
        }

        when (obj) {
            is Bundle -> {
                if (obj.isEmpty) {
                    map[prefix] = "Bundle{}"
                } else {
                    obj.keySet().forEach { key ->
                        if (key != "miui.focus.param") { // Skip the huge JSON
                            deepDump(obj.get(key), "$prefix.$key", map)
                        }
                    }
                }
            }
            is Array<*> -> {
                map[prefix] = "Array[${obj.size}]"
                obj.forEachIndexed { index, item ->
                    deepDump(item, "$prefix[$index]", map)
                }
            }
            is List<*> -> {
                map[prefix] = "List[${obj.size}]"
                obj.forEachIndexed { index, item ->
                    deepDump(item, "$prefix[$index]", map)
                }
            }

            // --- Specific Type Handling ---
            is Icon -> map[prefix] = "Icon(type=${obj.type}, pkg=${obj.resPackage}, id=${obj.resId})"
            is Bitmap -> map[prefix] = "Bitmap(${obj.width}x${obj.height})"
            is PendingIntent -> map[prefix] = "PendingIntent(creator=${obj.creatorPackage})"
            is Notification.Action -> {
                map["$prefix.title"] = obj.title.toString()
                deepDump(obj.extras, "$prefix.extras", map)
            }
            is CharSequence -> map[prefix] = obj.toString()
            is Number, is Boolean -> map[prefix] = obj.toString()

            // Primitive Arrays
            is IntArray -> map[prefix] = "IntArray${obj.contentToString()}"
            is LongArray -> map[prefix] = "LongArray${obj.contentToString()}"
            is ByteArray -> map[prefix] = "ByteArray[${obj.size}]"

            else -> map[prefix] = "${obj.javaClass.simpleName}: $obj"
        }
    }

    private fun getFlagNames(flags: Int): String {
        val active = mutableListOf<String>()
        if (flags and Notification.FLAG_SHOW_LIGHTS != 0) active.add("SHOW_LIGHTS")
        if (flags and Notification.FLAG_ONGOING_EVENT != 0) active.add("ONGOING_EVENT")
        if (flags and Notification.FLAG_INSISTENT != 0) active.add("INSISTENT")
        if (flags and Notification.FLAG_ONLY_ALERT_ONCE != 0) active.add("ONLY_ALERT_ONCE")
        if (flags and Notification.FLAG_AUTO_CANCEL != 0) active.add("AUTO_CANCEL")
        if (flags and Notification.FLAG_NO_CLEAR != 0) active.add("NO_CLEAR")
        if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) active.add("FOREGROUND_SERVICE")
        return active.joinToString(", ")
    }
}