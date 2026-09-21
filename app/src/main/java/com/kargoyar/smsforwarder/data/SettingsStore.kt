package com.kargoyar.smsforwarder.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Thin wrapper around SharedPreferences holding the user's forwarding configuration.
 */
object SettingsStore {

    private const val PREFS_NAME = "kargoyar_sms_forwarder_prefs"

    private const val KEY_SENDERS = "senders"
    private const val KEY_TEXT_FILTER = "text_filter"
    private const val KEY_FORWARD_NUMBERS = "forward_numbers"
    private const val KEY_WEBHOOK_URL = "webhook_url"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSenders(context: Context): String = prefs(context).getString(KEY_SENDERS, "") ?: ""

    fun getTextFilter(context: Context): String = prefs(context).getString(KEY_TEXT_FILTER, "") ?: ""

    fun getForwardNumbersRaw(context: Context): String =
        prefs(context).getString(KEY_FORWARD_NUMBERS, "") ?: ""

    fun getWebhookUrl(context: Context): String = prefs(context).getString(KEY_WEBHOOK_URL, "") ?: ""

    fun getForwardNumbers(context: Context): List<String> =
        getForwardNumbersRaw(context)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun save(
        context: Context,
        senders: String,
        textFilter: String,
        forwardNumbersRaw: String,
        webhookUrl: String
    ) {
        prefs(context).edit()
            .putString(KEY_SENDERS, senders)
            .putString(KEY_TEXT_FILTER, textFilter)
            .putString(KEY_FORWARD_NUMBERS, forwardNumbersRaw)
            .putString(KEY_WEBHOOK_URL, webhookUrl)
            .apply()
    }

    fun isForwardingConfigured(context: Context): Boolean =
        getForwardNumbers(context).isNotEmpty() || getWebhookUrl(context).isNotBlank()

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }
}
