package com.d4viddf.hyperisland_kit.demo

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationLogRepository {
    // Stores Live Data: Pair(Notification Data, Map of Images)
    private val _notifications = MutableStateFlow<List<Pair<InspectedNotification, Map<String, Bitmap>>>>(emptyList())
    val notifications = _notifications.asStateFlow()

    fun add(data: Pair<InspectedNotification, Map<String, Bitmap>>) {
        val current = _notifications.value.toMutableList()

        // Add new notification to the top
        current.add(0, data)

        // Limit buffer to 50 items to prevent OutOfMemory errors with large Bitmaps
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }

        _notifications.value = current
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}