package de.example.biometric

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val enableFingerprint by lazy { enable_fingerprint }

    private val fingerprintWrapped by lazy { FingerPrintWrapper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        enableFingerprint.setOnCheckedChangeListener { compoundButton, status ->
            fingerprintWrapped.saveUser("mobile@shore.com") {
                it?.let {
                    Toast.makeText(this@SettingsActivity,
                            it, Toast.LENGTH_LONG)
                            .show()
                }
            }
        }

        enableFingerprint.isChecked = fingerprintWrapped.getUser() != null
    }
}
