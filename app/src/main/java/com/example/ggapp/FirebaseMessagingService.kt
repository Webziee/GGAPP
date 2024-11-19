package com.example.ggapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


/*The following code makes use of firebase for sending push notifications. this code was
* inspired by the following video:
* SNATech, 2022. Youtube, How to send Android Push Notifications with Firebase Cloud Messaging. [Online]
Available at: https://www.youtube.com/watch?v=GDxj8KTmLrI
[Accessed 04 October 2024].*/
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        remoteMessage.notification?.let {
            // Show notification when a message is received
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, "booking_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(true)

        // If Android O or higher, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "booking_channel",
                "Booking Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
