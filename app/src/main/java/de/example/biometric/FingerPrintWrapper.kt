package de.example.biometric

import android.Manifest
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

@TargetApi(Build.VERSION_CODES.M)
class FingerPrintWrapper(private val context:Context) {

    private var cancellationSignal: CancellationSignal? = null

    private val KEY_NAME = "yourKey"

    lateinit var keyguardManager: KeyguardManager
    lateinit var fingerprintManager: FingerprintManager
    lateinit var keyStore: KeyStore
    lateinit var keyGenerator: KeyGenerator
    lateinit var cipher: Cipher
    lateinit var cryptoObject: FingerprintManager.CryptoObject

    interface OnSaveUserFingerPrint {
        fun onFingerprintSaved()
        fun onFingerprintError(error:String)
        fun onWaintingFingerprint()
    }


    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguardManager = context.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
            fingerprintManager = context.getSystemService(AppCompatActivity.FINGERPRINT_SERVICE) as FingerprintManager

            if (!fingerprintManager.isHardwareDetected()) {
//                textView.text = "Your device doesn't support fingerprint authentication"
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
//                textView.text = "Please enable the fingerprint permission"
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
//                textView.text = "No fingerprint configured. Please register at least one fingerprint in your device's Settings"
            }
        }

        if (!keyguardManager.isKeyguardSecure) {
            // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
//            textView.text = "Please enable lockscreen security in your device's Settings"
        } else {
            try {
                generateKey()
            } catch (e: FingerprintException) {
                e.printStackTrace()
            }
        }

        if (initCipher()) {
            cryptoObject = FingerprintManager.CryptoObject(cipher)
        }
    }

    fun getUser():String? {
        val mPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE)
        return mPrefs.getString("user", null)
    }

    fun saveUserFingerPrint(userName:String, onSaveUserFingerPrint:OnSaveUserFingerPrint) {
        val mPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()

        onSaveUserFingerPrint.onWaintingFingerprint()

        checkAuth(object :FingerprintHandler(context) {
            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                cancellationSignal?.cancel()
                onSaveUserFingerPrint.onFingerprintError("Authentication error\n$errString")
            }

            override fun onAuthenticationFailed() {
                cancellationSignal?.cancel()
                onSaveUserFingerPrint.onFingerprintError("Authentication failed")
            }

            override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                cancellationSignal?.cancel()
                onSaveUserFingerPrint.onFingerprintError("Authentication help\n$helpString")
            }

            override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                prefsEditor.putString("user", userName)
                prefsEditor.putString(userName, "dasdsa")
                prefsEditor.apply()
                onSaveUserFingerPrint.onFingerprintSaved()
            }
        })
    }

    fun checkUser(userName:String, callback:(error:String?) ->Unit) {
//        val mPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE)
//        val prefsEditor = mPrefs.edit()

        checkAuth(object :FingerprintHandler(context) {
            override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                cancellationSignal?.cancel()
                callback("Authentication error\n$errString")
            }

            override fun onAuthenticationFailed() {
                cancellationSignal?.cancel()
                callback("Authentication failed")
            }

            override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                cancellationSignal?.cancel()
                callback("Authentication help\n$helpString")
            }

            override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
//                prefsEditor.putString("user", userName)
//                prefsEditor.putString(userName, "dasdsa")
//                prefsEditor.apply()
                callback(null)
            }
        })
    }

    fun clear() {
        val mPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE)
        val prefsEditor = mPrefs.edit()
        prefsEditor.clear()
        prefsEditor.commit()
    }

    private fun checkAuth(fingerprintHandler: FingerprintHandler) {
        cancellationSignal = CancellationSignal()
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, fingerprintHandler, null)
    }

    @Throws(FingerprintException::class)
    private fun generateKey() {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore")

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            //Initialize an empty KeyStore//
            keyStore.load(null)

            //Initialize the KeyGenerator//
            keyGenerator.init(
                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                            //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(
                                    KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build())

            //Generate the key//
            keyGenerator.generateKey()

        } catch (exc: KeyStoreException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchAlgorithmException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: NoSuchProviderException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: InvalidAlgorithmParameterException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: CertificateException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        } catch (exc: IOException) {
            exc.printStackTrace()
            throw FingerprintException(exc)
        }
    }

    fun initCipher(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore.load(null)
            val key = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }

}

class FingerprintException(e: Exception) : Exception(e)

@TargetApi(Build.VERSION_CODES.M)
abstract class FingerprintHandler(private val context:Context) : FingerprintManager.AuthenticationCallback() {

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
    }

    override fun onAuthenticationFailed() {
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
    }
}