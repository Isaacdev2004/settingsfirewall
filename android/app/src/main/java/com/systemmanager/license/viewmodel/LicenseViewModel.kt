package com.systemmanager.license.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.systemmanager.license.data.model.LicenseActivationResponse
import com.systemmanager.license.data.model.LicenseValidationResponse
import com.systemmanager.license.data.repository.LicenseRepository
import com.systemmanager.license.utils.PreferenceManager
import kotlinx.coroutines.launch

class LicenseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferenceManager = PreferenceManager(application)
    private val repository = LicenseRepository(
        apiService = createApiService(),
        context = application,
        preferenceManager = preferenceManager
    )
    
    private val _activationResult = MutableLiveData<Result<LicenseActivationResponse>>()
    val activationResult: LiveData<Result<LicenseActivationResponse>> = _activationResult
    
    private val _validationResult = MutableLiveData<Result<LicenseValidationResponse>>()
    val validationResult: LiveData<Result<LicenseValidationResponse>> = _validationResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun activateLicense(licenseKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.activateLicense(licenseKey)
                _activationResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun validateLicense() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.validateLicense()
                _validationResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun isLicenseActivated(): Boolean {
        return preferenceManager.isActivated() && repository.isLicenseActive()
    }
    
    fun getLicenseStatus(): String? {
        return preferenceManager.getLicenseStatus()
    }
    
    fun getLicenseExpiry(): String? {
        return preferenceManager.getLicenseExpiry()
    }
    
    fun getDaysRemaining(): Int? {
        return repository.getDaysRemaining()
    }
    
    fun clearLicenseData() {
        repository.clearLicenseData()
    }
    
    private fun createApiService(): com.systemmanager.license.data.api.LicenseApiService {
        // This would be injected via DI in a real app
        // For now, we'll create it directly
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://your-backend-url.com/") // Replace with actual URL
            .addConverterFactory(com.squareup.retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        
        return retrofit.create(com.systemmanager.license.data.api.LicenseApiService::class.java)
    }
}
