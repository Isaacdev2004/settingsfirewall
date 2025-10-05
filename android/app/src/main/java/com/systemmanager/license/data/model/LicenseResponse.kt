package com.systemmanager.license.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for license activation
 */
data class LicenseActivationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("token")
    val token: String?,
    
    @SerializedName("license_status")
    val licenseStatus: String?,
    
    @SerializedName("expires_at")
    val expiresAt: String?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Response model for license validation
 */
data class LicenseValidationResponse(
    @SerializedName("valid")
    val valid: Boolean,
    
    @SerializedName("license_status")
    val licenseStatus: String?,
    
    @SerializedName("expires_at")
    val expiresAt: String?,
    
    @SerializedName("days_remaining")
    val daysRemaining: Int?,
    
    @SerializedName("error")
    val error: String?
)

/**
 * Request model for license activation
 */
data class LicenseActivationRequest(
    @SerializedName("license_key")
    val licenseKey: String,
    
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("device_info")
    val deviceInfo: String
)

/**
 * Request model for FCM token registration
 */
data class FCMTokenRequest(
    @SerializedName("device_id")
    val deviceId: String,
    
    @SerializedName("fcm_token")
    val fcmToken: String
)
