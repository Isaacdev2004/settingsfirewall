package com.systemmanager.license.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.systemmanager.license.databinding.ActivityMainBinding
import com.systemmanager.license.service.LicenseValidationWorker
import com.systemmanager.license.viewmodel.LicenseViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: LicenseViewModel
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Request notification permission
        requestNotificationPermission()
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[LicenseViewModel::class.java]
        
        // Setup UI
        setupUI()
        
        // Observe ViewModel
        observeViewModel()
        
        // Check if license is already activated
        if (viewModel.isLicenseActivated()) {
            showLicenseStatus()
        } else {
            showActivationForm()
        }
        
        // Schedule periodic license validation
        scheduleLicenseValidation()
    }
    
    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    private fun setupUI() {
        binding.btnActivate.setOnClickListener {
            val licenseKey = binding.etLicenseKey.text.toString().trim()
            if (licenseKey.isNotEmpty()) {
                viewModel.activateLicense(licenseKey)
            } else {
                Toast.makeText(this, "Please enter a license key", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnValidate.setOnClickListener {
            viewModel.validateLicense()
        }
        
        binding.btnRefresh.setOnClickListener {
            viewModel.validateLicense()
        }
    }
    
    private fun observeViewModel() {
        viewModel.activationResult.observe(this) { result ->
            result.fold(
                onSuccess = { response ->
                    Toast.makeText(this, "License activated successfully!", Toast.LENGTH_SHORT).show()
                    showLicenseStatus()
                },
                onFailure = { error ->
                    Toast.makeText(this, "Activation failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
        
        viewModel.validationResult.observe(this) { result ->
            result.fold(
                onSuccess = { response ->
                    Toast.makeText(this, "License is valid", Toast.LENGTH_SHORT).show()
                    updateLicenseStatus(response)
                },
                onFailure = { error ->
                    Toast.makeText(this, "Validation failed: ${error.message}", Toast.LENGTH_LONG).show()
                    if (error.message?.contains("expired") == true || 
                        error.message?.contains("revoked") == true) {
                        showActivationForm()
                    }
                }
            )
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnActivate.isEnabled = !isLoading
            binding.btnValidate.isEnabled = !isLoading
        }
    }
    
    private fun showActivationForm() {
        binding.layoutActivation.visibility = View.VISIBLE
        binding.layoutStatus.visibility = View.GONE
    }
    
    private fun showLicenseStatus() {
        binding.layoutActivation.visibility = View.GONE
        binding.layoutStatus.visibility = View.VISIBLE
        
        // Update status display
        val status = viewModel.getLicenseStatus()
        val expiry = viewModel.getLicenseExpiry()
        val daysRemaining = viewModel.getDaysRemaining()
        
        binding.tvStatus.text = "Status: ${status ?: "Unknown"}"
        binding.tvExpiry.text = "Expires: ${expiry ?: "Never"}"
        
        if (daysRemaining != null) {
            binding.tvDaysRemaining.text = "Days remaining: $daysRemaining"
            
            // Show warning if expiring soon
            if (daysRemaining <= 3) {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
                )
            } else if (daysRemaining <= 7) {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark)
                )
            } else {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                )
            }
        } else {
            binding.tvDaysRemaining.text = "No expiry date"
            binding.tvDaysRemaining.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_green_dark)
            )
        }
    }
    
    private fun updateLicenseStatus(response: com.systemmanager.license.data.model.LicenseValidationResponse) {
        binding.tvStatus.text = "Status: ${response.licenseStatus ?: "Unknown"}"
        binding.tvExpiry.text = "Expires: ${response.expiresAt ?: "Never"}"
        
        response.daysRemaining?.let { days ->
            binding.tvDaysRemaining.text = "Days remaining: $days"
            
            if (days <= 3) {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
                )
            } else if (days <= 7) {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_orange_dark)
                )
            } else {
                binding.tvDaysRemaining.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                )
            }
        }
    }
    
    private fun scheduleLicenseValidation() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val validationRequest = PeriodicWorkRequestBuilder<LicenseValidationWorker>(
            4, TimeUnit.HOURS, // Repeat every 4 hours
            1, TimeUnit.HOURS  // With 1 hour flex
        )
            .setConstraints(constraints)
            .addTag("license_validation")
            .build()
        
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "license_validation",
                ExistingPeriodicWorkPolicy.KEEP,
                validationRequest
            )
    }
}
