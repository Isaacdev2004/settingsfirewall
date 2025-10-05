package com.systemmanager.license.data.repository

import android.content.Context
import android.provider.Settings
import com.systemmanager.license.data.api.LicenseApiService
import com.systemmanager.license.data.model.*
import com.systemmanager.license.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for license management operations
 */
class LicenseRepository(
    private val apiService: LicenseApiService,
    private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    
    /**
     * Get unique device ID
     */
    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()
    }
    
    /**
     * Get device information
     */
    private fun getDeviceInfo(): String {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        val version = android.os.Build.VERSION.RELEASE
        val sdkInt = android.os.Build.VERSION.SDK_INT
        
        return "Android $version, $manufacturer $model, API Level $sdkInt"
    }
    
    /**
     * Activate a license
     */
    suspend fun activateLicense(licenseKey: String): Result<LicenseActivationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LicenseActivationRequest(
                    licenseKey = licenseKey,
                    deviceId = getDeviceId(),
                    deviceInfo = getDeviceInfo()
                )
                
                val response = apiService.activateLicense(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        // Store JWT token securely
                        body.token?.let { token ->
                            preferenceManager.saveJwtToken(token)
                        }
                        
                        // Store license information
                        body.licenseStatus?.let { status ->
                            preferenceManager.saveLicenseStatus(status)
                        }
                        
                        body.expiresAt?.let { expiresAt ->
                            preferenceManager.saveLicenseExpiry(expiresAt)
                        }
                        
                        Result.success(body)
                    } else {
                        Result.failure(Exception(body.error ?: "License activation failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Validate current license
     */
    suspend fun validateLicense(): Result<LicenseValidationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getJwtToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("No JWT token found"))
                }
                
                val response = apiService.validateLicense("Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.valid) {
                        // Update stored information
                        body.licenseStatus?.let { status ->
                            preferenceManager.saveLicenseStatus(status)
                        }
                        
                        body.expiresAt?.let { expiresAt ->
                            preferenceManager.saveLicenseExpiry(expiresAt)
                        }
                        
                        Result.success(body)
                    } else {
                        Result.failure(Exception(body.error ?: "License validation failed"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Register FCM token for push notifications
     */
    suspend fun registerFCMToken(fcmToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = preferenceManager.getJwtToken()
                if (token == null) {
                    return@withContext Result.failure(Exception("No JWT token found"))
                }
                
                val request = FCMTokenRequest(
                    deviceId = getDeviceId(),
                    fcmToken = fcmToken
                )
                
                val response = apiService.registerFCMToken("Bearer $token", request)
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Check if license is currently active
     */
    fun isLicenseActive(): Boolean {
        val status = preferenceManager.getLicenseStatus()
        val expiresAt = preferenceManager.getLicenseExpiry()
        
        if (status != "active") return false
        
        if (expiresAt != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val expiryDate = dateFormat.parse(expiresAt)
                val now = Date()
                
                return expiryDate?.after(now) ?: false
            } catch (e: Exception) {
                return false
            }
        }
        
        return true // No expiry date means license doesn't expire
    }
    
    /**
     * Get days remaining until license expires
     */
    fun getDaysRemaining(): Int? {
        val expiresAt = preferenceManager.getLicenseExpiry() ?: return null
        
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val expiryDate = dateFormat.parse(expiresAt)
            val now = Date()
            
            if (expiryDate != null) {
                val diffInMillis = expiryDate.time - now.time
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)
                return maxOf(0, diffInDays.toInt())
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        
        return null
    }
    
    /**
     * Clear all stored license data
     */
    fun clearLicenseData() {
        preferenceManager.clearLicenseData()
    }
}
