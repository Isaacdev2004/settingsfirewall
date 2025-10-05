package com.systemmanager.license.data.api

import com.systemmanager.license.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for license management
 */
interface LicenseApiService {
    
    /**
     * Activate a license for this device
     */
    @POST("activate")
    suspend fun activateLicense(
        @Body request: LicenseActivationRequest
    ): Response<LicenseActivationResponse>
    
    /**
     * Validate the current license
     */
    @POST("validate")
    suspend fun validateLicense(
        @Header("Authorization") token: String
    ): Response<LicenseValidationResponse>
    
    /**
     * Register FCM token for push notifications
     */
    @POST("register-fcm")
    suspend fun registerFCMToken(
        @Header("Authorization") token: String,
        @Body request: FCMTokenRequest
    ): Response<Unit>
}
