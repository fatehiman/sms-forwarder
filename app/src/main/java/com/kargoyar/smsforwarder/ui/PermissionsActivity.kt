package com.kargoyar.smsforwarder.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kargoyar.smsforwarder.R
import com.kargoyar.smsforwarder.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private val requestSmsPermissions =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) {
            refreshStatuses()
        }

    private val requestNotifPermission =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) {
            refreshStatuses()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.menu_permissions)

        binding.btnSms.setOnClickListener {
            requestSmsPermissions.launch(smsPermissions)
        }

        binding.btnNotif.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.btnBattery.setOnClickListener {
            requestIgnoreBatteryOptimizations()
        }

        binding.btnAutostart.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.groupNotif.visibility = android.view.View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatuses()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun refreshStatuses() {
        val smsGranted = smsPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        binding.statusSms.text = statusText(smsGranted)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            binding.statusNotif.text = statusText(notifGranted)
        }

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val batteryIgnored = powerManager.isIgnoringBatteryOptimizations(packageName)
        binding.statusBattery.text = statusText(batteryIgnored)
    }

    private fun statusText(granted: Boolean): String =
        getString(if (granted) R.string.status_granted else R.string.status_not_granted)

    private fun requestIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            refreshStatuses()
            return
        }
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Some OEMs restrict this intent; fall back to the app details screen.
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(fallback)
        }
    }
}
