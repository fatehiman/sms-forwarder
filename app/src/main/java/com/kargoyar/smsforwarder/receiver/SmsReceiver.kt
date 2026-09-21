package com.kargoyar.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kargoyar.smsforwarder.data.SettingsStore
import com.kargoyar.smsforwarder.util.MessageMatcher
import com.kargoyar.smsforwarder.worker.ForwardWorker
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val address = messages[0].originatingAddress
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val timestamp = messages[0].timestampMillis

        val senders = SettingsStore.getSenders(context)
        val textFilter = SettingsStore.getTextFilter(context)

        if (!MessageMatcher.matches(senders, textFilter, address, body)) return
        if (!SettingsStore.isForwardingConfigured(context)) return

        val request = OneTimeWorkRequestBuilder<ForwardWorker>()
            .setInputData(
                workDataOf(
                    ForwardWorker.KEY_FROM to (address ?: ""),
                    ForwardWorker.KEY_BODY to body,
                    ForwardWorker.KEY_TIMESTAMP to timestamp
                )
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueue(request)
    }
}
