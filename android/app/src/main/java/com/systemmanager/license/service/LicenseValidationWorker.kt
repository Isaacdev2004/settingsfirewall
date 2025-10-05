package com.systemmanager.license.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.systemmanager.license.R
import com.systemmanager.license.data.repository.LicenseRepository
import com.systemmanager.license.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LicenseValidationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val preferenceManager = PreferenceManager(context)
    private val repository = LicenseRepository(
        apiService = createApiService(),
        context = context,
        preferenceManager = preferenceManager
    )
    
    companion object {
        private const val CHANNEL_ID = "license_validation"
        private const val NOTIFICATION_ID = 2
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if license is activated
            if (!preferenceManager.isActivated()) {
                return@withContext Result.success()
            }
            
            // Validate license
            val result = repository.validateLicense()
            
            result.fold(
                onSuccess = { response ->
                    if (response.valid) {
                        // License is valid, check if expiring soon
                        response.daysRemaining?.let { days ->
                            if (days <= 3) {
                                showExpiryNotification(days)
                            }
                        }
                        Result.success()
                    } else {
                        // License is invalid, show notification
                        showInvalidLicenseNotification()
                        Result.success()
                    }
                },
                onFailure = { error ->
                    // Log error but don't fail the work
                    android.util.Log.e("LicenseValidation", "Validation failed", error)
                    Result.success()
                }
            )
        } catch (e: Exception) {
            android.util.Log.e("LicenseValidation", "Worker failed", e)
            Result.retry()
        }
    }
    
    private fun showExpiryNotification(daysRemaining: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "License Validation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for license validation results"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val title = "License Expiring Soon"
        val message = when (daysRemaining) {
            1 -> "Your license expires tomorrow!"
            0 -> "Your license expires today!"
            else -> "Your license expires in $daysRemaining days"
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showInvalidLicenseNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "License Validation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for license validation results"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("License Invalid")
            .setContentText("Your license has been revoked or expired. Please contact support.")
            .setSmallIcon(R.drawable.ic_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun createApiService(): com.systemmanager.license.data.api.LicenseApiService {
        // This would be injected via DI in a real app
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://your-backend-url.com/") // Replace with actual URL
            .addConverterFactory(com.squareup.retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        
        return retrofit.create(com.systemmanager.license.data.api.LicenseApiService::class.java)
    }
}
