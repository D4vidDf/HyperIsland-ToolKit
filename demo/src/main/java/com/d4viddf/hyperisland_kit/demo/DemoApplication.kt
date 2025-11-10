package com.d4viddf.hyperisland_kit.demo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class DemoApplication : Application() {

    companion object {
        const val DEMO_CHANNEL_ID = "hyperisland_demo_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.hyperisland_demos)
        val descriptionText = getString(R.string.channel_for_hyperisland_demo_notifications)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(DEMO_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}