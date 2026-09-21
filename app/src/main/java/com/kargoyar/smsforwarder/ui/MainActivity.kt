package com.kargoyar.smsforwarder.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kargoyar.smsforwarder.R
import com.kargoyar.smsforwarder.data.SettingsStore
import com.kargoyar.smsforwarder.databinding.ActivityMainBinding
import com.kargoyar.smsforwarder.service.ForwardService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val phoneNumberRegex = Regex("^\\+?[0-9]{4,15}$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        prefillFromSettings()

        binding.btnSave.setOnClickListener { onSaveClicked() }
    }

    private fun prefillFromSettings() {
        binding.editSenders.setText(SettingsStore.getSenders(this))
        binding.editTextFilter.setText(SettingsStore.getTextFilter(this))
        binding.editForwardNumbers.setText(SettingsStore.getForwardNumbersRaw(this))
        binding.editWebhook.setText(SettingsStore.getWebhookUrl(this))
    }

    private fun onSaveClicked() {
        binding.layoutForwardNumbers.error = null
        binding.layoutWebhook.error = null

        val senders = binding.editSenders.text?.toString().orEmpty()
        val textFilter = binding.editTextFilter.text?.toString().orEmpty()
        val forwardNumbersRaw = binding.editForwardNumbers.text?.toString().orEmpty()
        val webhookUrl = binding.editWebhook.text?.toString().orEmpty().trim()

        val forwardTokens = forwardNumbersRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val invalidNumber = forwardTokens.any { !phoneNumberRegex.matches(it) }
        if (invalidNumber) {
            binding.layoutForwardNumbers.error = getString(R.string.error_forward_numbers)
            return
        }

        if (webhookUrl.isNotEmpty()) {
            val validScheme = webhookUrl.startsWith("http://") || webhookUrl.startsWith("https://")
            val validUrl = validScheme && Patterns.WEB_URL.matcher(webhookUrl).matches()
            if (!validUrl) {
                binding.layoutWebhook.error = getString(R.string.error_webhook)
                return
            }
        }

        if (forwardTokens.isEmpty() && webhookUrl.isEmpty()) {
            Toast.makeText(this, R.string.error_no_destination, Toast.LENGTH_LONG).show()
            return
        }

        SettingsStore.save(this, senders, textFilter, forwardNumbersRaw, webhookUrl)

        if (SettingsStore.isForwardingConfigured(this)) {
            ContextCompat.startForegroundService(this, Intent(this, ForwardService::class.java))
        } else {
            stopService(Intent(this, ForwardService::class.java))
        }

        Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_permissions -> {
                startActivity(Intent(this, PermissionsActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
