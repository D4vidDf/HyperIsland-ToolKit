package com.d4viddf.hyperisland_kit.demo.data.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.d4viddf.hyperisland_kit.demo.data.parser.NotificationParser
import com.d4viddf.hyperisland_kit.demo.data.repository.NotificationLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HyperIslandListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "HyperListenerService"

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        fun requestReconnect(context: Context) {
            try {
                val component = ComponentName(context, HyperIslandListenerService::class.java)

                requestRebind(component)
                Log.d(TAG, "Requested rebind via NotificationListenerService.requestRebind()")

                val pm = context.packageManager
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Toggled component state to force Android to reconnect listener service")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to request listener reconnect", e)
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
        Log.d(TAG, "NotificationListenerService CONNECTED")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
        Log.d(TAG, "NotificationListenerService DISCONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            try {
                val result = NotificationParser.parse(this@HyperIslandListenerService, sbn)
                NotificationLogRepository.add(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional tracking
    }
}
