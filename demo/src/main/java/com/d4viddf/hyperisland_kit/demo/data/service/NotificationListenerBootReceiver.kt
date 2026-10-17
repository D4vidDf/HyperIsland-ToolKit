package com.d4viddf.hyperisland_kit.demo.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationListenerBootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ListenerBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "Received broadcast intent: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.PACKAGE_REPLACED" -> {
                Log.d(TAG, "Device boot or package update detected. Ensuring NotificationListener service is connected...")
                HyperIslandListenerService.ensureConnected(context)
            }
        }
    }
}
