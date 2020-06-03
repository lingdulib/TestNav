package cn.ling.yu.biometricsdk

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import javax.crypto.Cipher

/**
 * 展示生物识别对话框
 * @date 2020/4/24
 * @author Yu L.
 *
 */
object BiometricDialogUtils {

    private val mCryptographyManager: CryptographyManager= CryptographyManager()
    private var mBiometricCipherResultCallBack: BiometricCipherResultCallBack? = null

    fun showBiometricPromptForEncryption(ciphertextWrapper: CiphertextWrapper?,
        appCompatActivity: AppCompatActivity,
        decryption: Boolean,
        listenerFailer: (code: Int, errorMsg: String?) -> Unit
    ) {
        when (BiometricManager.from(appCompatActivity.applicationContext).canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                if (!decryption) {
                    auhtorBiometricEncryption(appCompatActivity)
                } else {
                    authorBiometricDecryptionOrNoValid(appCompatActivity,ciphertextWrapper)
                }
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                listenerFailer(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, "生物认证当前不可用.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                listenerFailer(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED, "该设备尚未创建任何生物特征.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                listenerFailer(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, "该设备无可用生物认证硬件.")
            }
        }
    }

    fun canFingerprint(appCompatActivity: AppCompatActivity):Boolean=BiometricManager.from(appCompatActivity.applicationContext).canAuthenticate()==BiometricManager.BIOMETRIC_SUCCESS

    private fun authorBiometricDecryptionOrNoValid(
        appCompatActivity: AppCompatActivity,
        ciphertextWrapper: CiphertextWrapper?
    ) {
        val secretKeyName = appCompatActivity.getString(R.string.secret_key_name)
        if (ciphertextWrapper != null) {
            val cipher = mCryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, ciphertextWrapper.initializationVector
            )
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    appCompatActivity,
                    ::decryptionAndStoreLocal
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(appCompatActivity)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }else{
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    appCompatActivity,
                    ::normalValidCallBack
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(appCompatActivity)
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun auhtorBiometricEncryption(appCompatActivity: AppCompatActivity) {
        val secretKeyName = appCompatActivity.getString(R.string.secret_key_name)
        val cipher = mCryptographyManager.getInitializedCipherForEncryption(secretKeyName)
        val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
            appCompatActivity,
            ::encryptAndStoreServer
        )
        val promptInfo = BiometricPromptUtils.createPromptInfo(appCompatActivity)
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun normalValidCallBack(
        authResult: BiometricPrompt.AuthenticationResult?,
        code: Int,
        error: String?
    ){
        mBiometricCipherResultCallBack?.obtainErrorInfo(code,error)
    }

    private fun decryptionAndStoreLocal(
        authResult: BiometricPrompt.AuthenticationResult?,
        code: Int,
        error: String?
    ) {
        if (authResult == null) {
            mBiometricCipherResultCallBack?.obtainErrorInfo(code, error)
        } else {
            authResult.cryptoObject?.cipher?.let {
                mBiometricCipherResultCallBack?.obtainDCipher(it)
            }
        }
    }

    private fun encryptAndStoreServer(
        authResult: BiometricPrompt.AuthenticationResult?,
        code: Int,
        error: String?
    ) {
        if (authResult == null) {
            mBiometricCipherResultCallBack?.obtainErrorInfo(code, error)
        } else {
            authResult.cryptoObject?.cipher?.let {
                mBiometricCipherResultCallBack?.obtainCipher(it)
            }
        }
    }

    /**
     * 加密数据
     */
    fun saveCiphertextWrapper(text: String, cipher: Cipher, appCompatActivity: AppCompatActivity) {
        val encryptedServerTokenWrapper = mCryptographyManager.encryptData(text, cipher)
        mCryptographyManager.persistCiphertextWrapperToSharedPrefs(
            encryptedServerTokenWrapper,
            appCompatActivity.applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )
    }

    fun setBiometricCipherResultCallBack(biometricCipherResultCallBack: BiometricCipherResultCallBack?) {
        mBiometricCipherResultCallBack = biometricCipherResultCallBack
    }

    fun getCiphertextWrapper(appCompatActivity: AppCompatActivity): CiphertextWrapper? {
        return mCryptographyManager.getCiphertextWrapperFromSharedPrefs(
            appCompatActivity.applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )
    }

    fun getCryptographyManager():CryptographyManager?= mCryptographyManager
}