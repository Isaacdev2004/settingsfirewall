package com.systemmanager.license.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure preference manager using EncryptedSharedPreferences
 */
class PreferenceManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "license_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_LICENSE_STATUS = "license_status"
        private const val KEY_LICENSE_EXPIRY = "license_expiry"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LAST_VALIDATION = "last_validation"
        private const val KEY_IS_ACTIVATED = "is_activated"
    }
    
    /**
     * Save JWT token securely
     */
    fun saveJwtToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_JWT_TOKEN, token)
            .apply()
    }
    
    /**
     * Get JWT token
     */
    fun getJwtToken(): String? {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null)
    }
    
    /**
     * Save license status
     */
    fun saveLicenseStatus(status: String) {
        sharedPreferences.edit()
            .putString(KEY_LICENSE_STATUS, status)
            .apply()
    }
    
    /**
     * Get license status
     */
    fun getLicenseStatus(): String? {
        return sharedPreferences.getString(KEY_LICENSE_STATUS, null)
    }
    
    /**
     * Save license expiry date
     */
    fun saveLicenseExpiry(expiryDate: String) {
        sharedPreferences.edit()
            .putString(KEY_LICENSE_EXPIRY, expiryDate)
            .apply()
    }
    
    /**
     * Get license expiry date
     */
    fun getLicenseExpiry(): String? {
        return sharedPreferences.getString(KEY_LICENSE_EXPIRY, null)
    }
    
    /**
     * Save FCM token
     */
    fun saveFCMToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }
    
    /**
     * Get FCM token
     */
    fun getFCMToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }
    
    /**
     * Save last validation timestamp
     */
    fun saveLastValidation(timestamp: Long) {
        sharedPreferences.edit()
            .putLong(KEY_LAST_VALIDATION, timestamp)
            .apply()
    }
    
    /**
     * Get last validation timestamp
     */
    fun getLastValidation(): Long {
        return sharedPreferences.getLong(KEY_LAST_VALIDATION, 0)
    }
    
    /**
     * Set activation status
     */
    fun setActivated(isActivated: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_ACTIVATED, isActivated)
            .apply()
    }
    
    /**
     * Check if license is activated
     */
    fun isActivated(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ACTIVATED, false)
    }
    
    /**
     * Clear all license data
     */
    fun clearLicenseData() {
        sharedPreferences.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_LICENSE_STATUS)
            .remove(KEY_LICENSE_EXPIRY)
            .remove(KEY_LAST_VALIDATION)
            .remove(KEY_IS_ACTIVATED)
            .apply()
    }
    
    /**
     * Clear all data
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
