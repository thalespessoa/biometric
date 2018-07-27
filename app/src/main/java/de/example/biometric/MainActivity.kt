package de.example.biometric

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import de.example.biometric.drawables.BackgroundDrawable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val backgroundImageView by lazy { background_image_view }
    private val signInButton by lazy { button_login }
    private val fingerprintContainer by lazy { fingerprint }

    private val fingerprintWrapped by lazy { FingerPrintWrapper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        backgroundImageView.setImageDrawable(BackgroundDrawable())

        signInButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        fingerprintContainer.visibility = fingerprintWrapped.getUser()?.let { View.VISIBLE } ?: View.INVISIBLE
    }
}
