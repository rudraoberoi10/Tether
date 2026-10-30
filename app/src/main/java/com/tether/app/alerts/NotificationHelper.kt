package com.tether.app.alerts

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tether.app.MainActivity
import com.tether.app.R
import com.tether.app.TetherApp

class NotificationHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun showExitNotification(
        circleId: String,
        circleName: String,
        userName: String,
        fenceLabel: String,
        eventId: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w("NotificationHelper", "POST_NOTIFICATIONS not granted! Cannot post notification.")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CIRCLE_ID, circleId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            circleId.hashCode(),
            intent,
            pendingIntentFlags
        )

        val title = "$userName left $circleName"
        val body = "Moved outside the $fenceLabel fence. Tap to view."

        val notification = NotificationCompat.Builder(context, TetherApp.CHANNEL_EXIT_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(eventId.hashCode(), notification)
        Log.i("NotificationHelper", "POSTED NOTIFICATION: $title")
    }

    companion object {
        const val EXTRA_CIRCLE_ID = "extra_circle_id"
    }
}