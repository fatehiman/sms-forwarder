package com.kargoyar.smsforwarder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kargoyar.smsforwarder.BuildConfig
import com.kargoyar.smsforwarder.R
import com.kargoyar.smsforwarder.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.menu_about)

        binding.textAppName.text = getString(R.string.app_name)
        binding.textVersion.text = getString(R.string.about_version_format, BuildConfig.VERSION_NAME)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
