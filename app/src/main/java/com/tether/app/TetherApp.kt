package com.tether.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class TetherApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
        // Begin watching for member exit events in the background
        container.exitAlertWatcher.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_EXIT_ALERTS,
                "Exit alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when members exit a circle fence"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_EXIT_ALERTS = "exit_alerts"
    }
}