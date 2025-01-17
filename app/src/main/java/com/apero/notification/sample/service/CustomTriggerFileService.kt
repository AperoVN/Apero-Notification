package com.apero.notification.sample.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.apero.notification.getNotificationManager
import com.apero.notification.sample.R
import com.apero.triggerfile.services.TriggerFileService
import java.io.File

class CustomTriggerFileService : TriggerFileService() {
    companion object {
        const val NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL_SERVICE"
    }

    override fun onNewFileDetected(file: File) {
        Log.d("CustomTriggerFileService", "onNewFileDetected: $file")
    }

    override fun createNotificationService(): Notification {
        val notificationManager = this.getNotificationManager()

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle("This notification for trigger service")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                this.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        return builder.build()
    }
}