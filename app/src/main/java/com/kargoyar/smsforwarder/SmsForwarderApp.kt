package com.kargoyar.smsforwarder

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SmsForwarderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fa"))
    }
}
