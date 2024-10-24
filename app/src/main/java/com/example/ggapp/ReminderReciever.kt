package com.example.ggapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val bookingId = intent?.getStringExtra("booking_id")

        // Show notification for the booking reminder
        showNotification(context, bookingId)
    }

    private fun showNotification(context: Context?, bookingId: String?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "booking_channel"

        // Create the notification channel if the API level is 26 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Booking Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context!!, channelId)
            .setContentTitle("Booking Reminder")
            .setContentText("Don't forget your booking is coming up!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        notificationManager.notify(bookingId.hashCode(), notificationBuilder.build())
    }
}
