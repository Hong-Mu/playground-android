package com.hongmu.serviceexample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "my_foreground_service_channel"
        const val CHANNEL_NAME = "Foreground Service Channel"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            NotificationManagerCompat.from(this).apply {
                createNotificationChannel(channel)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Running...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()

        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }
}