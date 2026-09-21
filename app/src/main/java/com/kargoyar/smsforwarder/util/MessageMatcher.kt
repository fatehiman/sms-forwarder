package com.kargoyar.smsforwarder.util

import java.util.Locale

/**
 * Pure matching logic for deciding whether an incoming SMS should be forwarded.
 * Kept free of Android framework dependencies so it can be unit-tested directly.
 */
object MessageMatcher {

    /**
     * @param sendersFilterRaw raw, comma-separated "senders" field from settings.
     * @param textFilterRaw raw "text filter" field from settings.
     * @param address the originating address (phone number or sender id) of the incoming SMS.
     * @param body the full concatenated body of the incoming SMS.
     */
    fun matches(
        sendersFilterRaw: String,
        textFilterRaw: String,
        address: String?,
        body: String?
    ): Boolean {
        return sendersMatch(sendersFilterRaw, address) && textMatches(textFilterRaw, body)
    }

    fun sendersMatch(sendersFilterRaw: String, address: String?): Boolean {
        val tokens = parseCsv(sendersFilterRaw)
        if (tokens.isEmpty()) return true
        val addr = address ?: return false
        return tokens.any { token -> matchesSingleSender(token, addr) }
    }

    fun textMatches(textFilterRaw: String, body: String?): Boolean {
        val filter = textFilterRaw.trim()
        if (filter.isBlank()) return true
        val text = body ?: return false
        return text.lowercase(Locale.ROOT).contains(filter.lowercase(Locale.ROOT))
    }

    fun parseCsv(raw: String): List<String> =
        raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun matchesSingleSender(token: String, address: String): Boolean {
        // A token is "numeric" if, once we strip an optional leading '+', everything else is digits.
        val tokenWithoutPlus = if (token.startsWith("+")) token.substring(1) else token
        val looksNumeric = tokenWithoutPlus.isNotEmpty() && tokenWithoutPlus.all { it.isDigit() }

        if (!looksNumeric) {
            return token.equals(address, ignoreCase = true)
        }

        val tokenDigits = token.filter { it.isDigit() }
        val addressDigits = address.filter { it.isDigit() }
        if (tokenDigits.isEmpty() || addressDigits.isEmpty()) return false

        // Compare using the last 7 digits (or fewer, if one side is shorter) to tolerate
        // country-code / leading-zero differences, e.g. "09121234567" vs "+989121234567".
        val compareLen = minOf(7, tokenDigits.length, addressDigits.length)
        if (compareLen <= 0) return false

        return tokenDigits.takeLast(compareLen) == addressDigits.takeLast(compareLen)
    }
}
