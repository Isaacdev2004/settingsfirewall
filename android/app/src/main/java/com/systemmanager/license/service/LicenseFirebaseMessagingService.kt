package com.systemmanager.license.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.systemmanager.license.R
import com.systemmanager.license.ui.MainActivity
import com.systemmanager.license.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LicenseFirebaseMessagingService : FirebaseMessagingService() {
    
    private val preferenceManager by lazy { PreferenceManager(this) }
    
    companion object {
        private const val CHANNEL_ID = "license_notifications"
        private const val NOTIFICATION_ID = 1
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle data payload
        remoteMessage.data.let { data ->
            val type = data["type"]
            val title = remoteMessage.notification?.title ?: "License Notification"
            val body = remoteMessage.notification?.body ?: "You have a new notification"
            
            when (type) {
                "license_revoked" -> {
                    handleLicenseRevoked(data, title, body)
                }
                "license_expiring" -> {
                    handleLicenseExpiring(data, title, body)
                }
                "admin_message" -> {
                    handleAdminMessage(data, title, body)
                }
                else -> {
                    showNotification(title, body)
                }
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Save the new token
        preferenceManager.saveFCMToken(token)
        
        // Register the token with the backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // This would typically be done through a repository
                // For now, we'll just log it
                android.util.Log.d("FCM", "New token: $token")
            } catch (e: Exception) {
                android.util.Log.e("FCM", "Failed to register token", e)
            }
        }
    }
    
    private fun handleLicenseRevoked(data: Map<String, String>, title: String, body: String) {
        // Clear license data when revoked
        preferenceManager.clearLicenseData()
        
        // Show notification
        showNotification(title, body)
        
        // You could also show a dialog or redirect to activation screen
        // For now, we'll just show a notification
    }
    
    private fun handleLicenseExpiring(data: Map<String, String>, title: String, body: String) {
        // Show notification about expiring license
        showNotification(title, body)
        
        // You could also show a local notification reminder
        scheduleExpiryReminder(data)
    }
    
    private fun handleAdminMessage(data: Map<String, String>, title: String, body: String) {
        // Show admin message notification
        showNotification(title, body)
    }
    
    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun scheduleExpiryReminder(data: Map<String, String>) {
        // You could schedule a local notification reminder here
        // This is just a placeholder
        android.util.Log.d("FCM", "Scheduling expiry reminder for license: ${data["license_key"]}")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "License Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for license events and admin messages"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
