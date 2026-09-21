package com.kargoyar.smsforwarder

import com.kargoyar.smsforwarder.util.MessageMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageMatcherTest {

    @Test
    fun emptySendersFilterMatchesEverything() {
        assertTrue(MessageMatcher.sendersMatch("", "+989121234567"))
        assertTrue(MessageMatcher.sendersMatch("   ", "anything"))
    }

    @Test
    fun numericSenderMatchesWithCountryCodeDifference() {
        assertTrue(MessageMatcher.sendersMatch("09121234567", "+989121234567"))
        assertTrue(MessageMatcher.sendersMatch("+989121234567", "09121234567"))
    }

    @Test
    fun numericSenderDoesNotMatchDifferentNumber() {
        assertFalse(MessageMatcher.sendersMatch("09121234567", "+989129999999"))
    }

    @Test
    fun nameSenderMatchesExactCaseInsensitive() {
        assertTrue(MessageMatcher.sendersMatch("BankX", "bankx"))
        assertFalse(MessageMatcher.sendersMatch("BankX", "BankY"))
    }

    @Test
    fun multipleTokensAnyMayMatch() {
        assertTrue(MessageMatcher.sendersMatch("09121234567, BankX", "bankx"))
    }

    @Test
    fun emptyTextFilterMatchesEverything() {
        assertTrue(MessageMatcher.textMatches("", "any body"))
    }

    @Test
    fun textFilterIsCaseInsensitiveSubstring() {
        assertTrue(MessageMatcher.textMatches("code", "Your CODE is 1234"))
        assertFalse(MessageMatcher.textMatches("code", "no match here"))
    }

    @Test
    fun overallMatchRequiresBothSendersAndText() {
        assertTrue(
            MessageMatcher.matches("09121234567", "code", "+989121234567", "your code is 1")
        )
        assertFalse(
            MessageMatcher.matches("09121234567", "code", "+989121234567", "no relevant text")
        )
        assertFalse(
            MessageMatcher.matches("09121234567", "code", "+989129999999", "your code is 1")
        )
    }
}
