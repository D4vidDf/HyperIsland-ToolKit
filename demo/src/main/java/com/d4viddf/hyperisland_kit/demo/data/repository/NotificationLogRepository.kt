package com.d4viddf.hyperisland_kit.demo.data.repository

import android.graphics.Bitmap
import com.d4viddf.hyperisland_kit.demo.data.model.InspectedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationLogRepository {
    private val _notifications = MutableStateFlow<List<Pair<InspectedNotification, Map<String, Bitmap>>>>(emptyList())
    val notifications: StateFlow<List<Pair<InspectedNotification, Map<String, Bitmap>>>> = _notifications.asStateFlow()

    fun add(data: Pair<InspectedNotification, Map<String, Bitmap>>) {
        val current = _notifications.value.toMutableList()
        current.add(0, data)
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        _notifications.value = current
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
