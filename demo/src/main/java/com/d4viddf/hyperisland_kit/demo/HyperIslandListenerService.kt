package com.d4viddf.hyperisland_kit.demo

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HyperIslandListenerService : NotificationListenerService() {

    // Use Default dispatcher for CPU-intensive parsing (JSON + Bitmaps)
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            try {
                // 1. Parse the notification (returns Pair<InspectedNotification, Map<String, Bitmap>>)
                val result = NotificationParser.parse(this@HyperIslandListenerService, sbn)

                // 2. Add to Live Repository
                NotificationLogRepository.add(result)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: If you want to track removed notifications later
    }
}