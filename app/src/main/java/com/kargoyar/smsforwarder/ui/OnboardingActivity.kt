package com.kargoyar.smsforwarder.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kargoyar.smsforwarder.R
import com.kargoyar.smsforwarder.data.SettingsStore
import com.kargoyar.smsforwarder.databinding.ActivityOnboardingBinding

/**
 * First-run wizard: walks the user through granting each permission one at a time,
 * since many customers don't know how to use Android's permission dialogs on their own.
 * Runs once; afterwards MainActivity is the entry point.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private val steps: List<Step> by lazy { buildSteps() }
    private var stepIndex = 0

    private val requestSmsPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            goToNextStep()
        }

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            goToNextStep()
        }

    private val batterySettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            goToNextStep()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SettingsStore.isOnboardingDone(this)) {
            goToMain()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showStep(0)
    }

    private sealed class Step {
        object Welcome : Step()
        object Sms : Step()
        object Notifications : Step()
        object Battery : Step()
        object Finish : Step()
    }

    private fun buildSteps(): List<Step> {
        val list = mutableListOf<Step>(Step.Welcome, Step.Sms)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Step.Notifications)
        }
        list.add(Step.Battery)
        list.add(Step.Finish)
        return list
    }

    private fun goToNextStep() {
        showStep(stepIndex + 1)
    }

    private fun showStep(index: Int) {
        if (index >= steps.size) {
            finishOnboarding()
            return
        }
        stepIndex = index
        binding.stepStatus.text = ""
        binding.btnSkip.visibility = android.view.View.INVISIBLE

        when (steps[index]) {
            Step.Welcome -> {
                binding.stepTitle.text = getString(R.string.onboarding_welcome_title)
                binding.stepDescription.text = getString(R.string.onboarding_welcome_desc)
                binding.btnPrimary.text = getString(R.string.onboarding_start)
                binding.btnPrimary.setOnClickListener { goToNextStep() }
            }
            Step.Sms -> {
                binding.stepTitle.text = getString(R.string.perm_sms_title)
                binding.stepDescription.text = getString(R.string.perm_sms_desc)
                binding.btnPrimary.text = getString(R.string.btn_perm_sms)
                binding.btnPrimary.setOnClickListener { requestSmsPermissions.launch(smsPermissions) }
                binding.btnSkip.visibility = android.view.View.VISIBLE
                binding.btnSkip.setOnClickListener { goToNextStep() }
            }
            Step.Notifications -> {
                binding.stepTitle.text = getString(R.string.perm_notif_title)
                binding.stepDescription.text = getString(R.string.perm_notif_desc)
                binding.btnPrimary.text = getString(R.string.btn_perm_notif)
                binding.btnPrimary.setOnClickListener {
                    requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                binding.btnSkip.visibility = android.view.View.VISIBLE
                binding.btnSkip.setOnClickListener { goToNextStep() }
            }
            Step.Battery -> {
                binding.stepTitle.text = getString(R.string.perm_battery_title)
                binding.stepDescription.text = getString(R.string.perm_battery_desc)
                binding.btnPrimary.text = getString(R.string.btn_perm_battery)
                binding.btnPrimary.setOnClickListener { requestIgnoreBatteryOptimizations() }
                binding.btnSkip.visibility = android.view.View.VISIBLE
                binding.btnSkip.setOnClickListener { goToNextStep() }
            }
            Step.Finish -> {
                binding.stepTitle.text = getString(R.string.onboarding_finish_title)
                binding.stepDescription.text = getString(R.string.onboarding_finish_desc)
                binding.btnPrimary.text = getString(R.string.onboarding_enter_app)
                binding.btnPrimary.setOnClickListener { finishOnboarding() }
            }
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            goToNextStep()
            return
        }
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            batterySettingsLauncher.launch(intent)
        } catch (e: Exception) {
            goToNextStep()
        }
    }

    private fun finishOnboarding() {
        SettingsStore.setOnboardingDone(this)
        goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
