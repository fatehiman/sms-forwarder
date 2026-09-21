package com.kargoyar.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kargoyar.smsforwarder.data.SettingsStore
import com.kargoyar.smsforwarder.service.ForwardService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        if (SettingsStore.isForwardingConfigured(context)) {
            val serviceIntent = Intent(context, ForwardService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
