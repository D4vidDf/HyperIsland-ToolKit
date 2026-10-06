package com.d4viddf.hyperisland_kit.demo.data.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotifActionReceiver"

        const val ACTION_CLICK_AND_CANCEL = "com.d4viddf.hyperisland_kit.demo.ACTION_CLICK_AND_CANCEL"
        const val ACTION_SHOW_TOAST_ONLY = "com.d4viddf.hyperisland_kit.demo.ACTION_SHOW_TOAST_ONLY"

        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TOAST_MESSAGE = "toast_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered with action: ${intent.action}")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId == -1) {
            Log.e(TAG, "Invalid Notification ID (-1). Aborting.")
            return
        }

        val toastMessage = intent.getStringExtra(EXTRA_TOAST_MESSAGE)

        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }

        if (intent.action == ACTION_CLICK_AND_CANCEL) {
            notificationManager.cancel(notificationId)
        }
    }
}
