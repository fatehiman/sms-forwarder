package com.kargoyar.smsforwarder.worker

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kargoyar.smsforwarder.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import android.util.Log

class ForwardWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_FROM = "from"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "ForwardWorker"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 15_000
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val from = inputData.getString(KEY_FROM).orEmpty()
        val body = inputData.getString(KEY_BODY).orEmpty()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val numbers = SettingsStore.getForwardNumbers(applicationContext)
        val webhookUrl = SettingsStore.getWebhookUrl(applicationContext)

        var webhookNeedsRetry = false

        if (numbers.isNotEmpty()) {
            forwardBySms(from, body, numbers)
        }

        if (webhookUrl.isNotBlank()) {
            webhookNeedsRetry = !postWebhook(webhookUrl, from, body, timestamp)
        }

        if (webhookNeedsRetry) Result.retry() else Result.success()
    }

    private fun forwardBySms(from: String, body: String, numbers: List<String>) {
        val hasSendPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasSendPermission) {
            Log.w(TAG, "SEND_SMS permission not granted, skipping SMS resend")
            return
        }

        val smsManager = try {
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                applicationContext.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not obtain SmsManager", e)
            null
        } ?: return

        val textToSend = "از طرف $from:\n$body"

        for (number in numbers) {
            try {
                val parts = smsManager.divideMessage(textToSend)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to forward SMS to $number", e)
            }
        }
    }

    /** Returns true on success (2xx), false if it should be retried. */
    private fun postWebhook(webhookUrl: String, from: String, body: String, timestampMillis: Long): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            val json = JSONObject()
                .put("from", from)
                .put("message", body)
                .put("receivedAt", Instant.ofEpochMilli(timestampMillis).toString())

            val url = URL(webhookUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(json.toString())
            }

            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Webhook POST failed", e)
            false
        } finally {
            connection?.disconnect()
        }
    }
}
