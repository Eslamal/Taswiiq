package com.example.taswiiq

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.taswiiq.data.TaswiiqRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val repository = TaswiiqRepository()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token generated: $token")
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            sendTokenToServer(userId, token)
        }
    }

    /**
     * --- MODIFIED HERE ---
     * This function now extracts the custom data payload from the message.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "New Notification"
        val body = message.notification?.body ?: "You have a new message."

        // Extract custom data sent from the Cloud Function
        val data = message.data
        val screen = data["screen"] // e.g., "orders"
        val referenceId = data["referenceId"] // e.g., the orderId

        Log.d("FCM", "Message Received: Title: $title, Body: $body, Screen: $screen, RefID: $referenceId")

        // Pass the data to the function that shows the notification
        showNotification(title, body, screen, referenceId)
    }

    private fun sendTokenToServer(userId: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateUserFcmToken(userId, token)
                .onSuccess { Log.d("FCM", "Token updated successfully for user: $userId") }
                .onFailure { e -> Log.e("FCM", "Failed to update token: ${e.message}") }
        }
    }

    /**
     * --- MODIFIED HERE ---
     * This function now creates a PendingIntent to make the notification clickable.
     */
    private fun showNotification(title: String, body: String, screen: String?, referenceId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "taswiiq_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Taswiiq Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Taswiiq app notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add the navigation data as extras
            putExtra("screen", screen)
            putExtra("referenceId", referenceId)
        }

        // Create a PendingIntent that wraps the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code
            intent,
            // Flag ensures the intent is updated with new data, and is immutable for security
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent

        // Show the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}