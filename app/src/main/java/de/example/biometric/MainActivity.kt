package de.example.biometric

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import de.example.biometric.drawables.BackgroundDrawable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val backgroundImageView by lazy { background_image_view }
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        backgroundImageView.setImageDrawable(BackgroundDrawable())

        textView = findViewById(R.id.text_view)

        FingerPrintWrapper(this).saveUserFingerPrint("asdasd", object:FingerPrintWrapper.OnSaveUserFingerPrint {
            override fun onFingerprintError(error: String) {
                println("----- onFingerprintError: "+error)
            }

            override fun onFingerprintSaved() {
                println("----- onFingerprintSaved")
            }

            override fun onWaintingFingerprint() {
                println("----- onWaintingFingerprint")
            }
        })
    }

}
