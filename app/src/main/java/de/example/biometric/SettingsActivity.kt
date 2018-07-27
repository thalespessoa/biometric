package de.example.biometric

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private val enableFingerprint by lazy { enable_fingerprint }
    private val touchDialog by lazy { dialog_fingerprint }

    private val fingerprintInstructions by lazy { fingerprint_instructions }
    private val fingerprintIcon by lazy { fingerprint_icon }
    private val fingerprintStatus by lazy { fingerprint_status }

    private val fingerprintWrapped by lazy { FingerPrintWrapper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        enableFingerprint.isChecked = fingerprintWrapped.getUser() != null
        enableFingerprint.setOnCheckedChangeListener { compoundButton, status ->

            if (status) {
                fingerprintWrapped.saveUserFingerPrint("mobile@shore.com", object:FingerPrintWrapper.OnSaveUserFingerPrint {
                    override fun onFingerprintError(error: String) {
                        fingerprintStatus.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_block))
                        fingerprintStatus.visibility = View.VISIBLE
                        fingerprintInstructions.text = "This fingerprint is invalid"
                        fingerprintIcon.alpha = 0.5f

                        enableFingerprint.isChecked = false
                        closeDialog()
                    }

                    override fun onFingerprintSaved() {
                        fingerprintStatus.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_check))
                        fingerprintStatus.visibility = View.VISIBLE
                        fingerprintInstructions.text = "This fingerprint is valid"
                        fingerprintIcon.alpha = 0.5f
                        closeDialog()
                    }

                    override fun onWaintingFingerprint() {
                        touchDialog.visibility = View.VISIBLE
                    }
                })
            }
        }
    }

    private fun closeDialog() {
        val h = Handler()
        h.postDelayed({
            touchDialog.visibility = View.GONE
        }, 4000)
    }
}
