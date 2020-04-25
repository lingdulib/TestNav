/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.net.testnav.biometric

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import cn.com.net.testnav.R
import cn.com.net.testnav.databinding.ActivityLoginBinding
import cn.ling.yu.biometricsdk.BiometricCipherResultCallBack
import cn.ling.yu.biometricsdk.BiometricDialogUtils
import cn.ling.yu.biometricsdk.CiphertextWrapper
import com.google.android.material.snackbar.Snackbar
import javax.crypto.Cipher


/**
 * 1) after entering "valid" username and password, login button becomes enabled
 * 2) User clicks biometrics?
 *   - a) if no template exists, then ask user to register template
 *   - b) if template exists, ask user to confirm by entering username & password
 */
class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var biometricPrompt: BiometricPrompt
    private var ciphertextWrapper:CiphertextWrapper?=null
    private lateinit var binding: ActivityLoginBinding
    private val loginWithPasswordViewModel by viewModels<LoginWithPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ciphertextWrapper==BiometricDialogUtils.getCiphertextWrapper(this)
        binding.useBiometrics.setOnClickListener {
            if (ciphertextWrapper != null) {
                showBiometricPromptForDecryption()
            } else {
                startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
            }
        }
        if (ciphertextWrapper == null) {
            setupForLoginWithPassword()
        }
    }

    /**
     * The logic is kept inside onResume instead of onCreate so that authorizing biometrics takes
     * immediate effect.
     */
    override fun onResume() {
        super.onResume()

        if (ciphertextWrapper != null) {
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                // The user has already logged in, so proceed to the rest of the app
                // this is a todo for you, the developer
                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    // BIOMETRICS SECTION

    private fun showBiometricPromptForDecryption() {
         BiometricDialogUtils.setBiometricCipherResultCallBack(object:BiometricCipherResultCallBack{
             override fun obtainCipher(cipher: Cipher) {

             }

             override fun obtainDCipher(cipher: Cipher) {
                 ciphertextWrapper?.let { textWrapper ->
                        cipher.let {
                         val plaintext =
                             BiometricDialogUtils.getCryptographyManager()?.decryptData(textWrapper.ciphertext, it)
                         SampleAppUser.fakeToken = plaintext
                         // Now that you have the token, you can query server for everything else
                         // the only reason we call this fakeToken is because we didn't really get it from
                         // the server. In your case, you will have gotten it from the server the first time
                         // and therefore, it's a real token.

                         updateApp(getString(R.string.already_signedin))
                     }
                 }
             }

             override fun obtainErrorInfo(errCode: Int, errorMsg: String?) {
                 Snackbar.make(window.decorView,"$errCode==$errorMsg",Snackbar.LENGTH_INDEFINITE).setAction("确定",null).show()
             }
         })
        BiometricDialogUtils.showBiometricPromptForEncryption(ciphertextWrapper,this,true,::failResultCallBack)
    }

    private fun failResultCallBack(code:Int,error:String?){
        Snackbar.make(window.decorView,error?:"认证失败.",Snackbar.LENGTH_LONG).setAction("确定",null).show()
    }

    // USERNAME + PASSWORD SECTION

    private fun setupForLoginWithPassword() {
        loginWithPasswordViewModel.loginWithPasswordFormState.observe(this, Observer { formState ->
            val loginState = formState ?: return@Observer
            when (loginState) {
                is SuccessfulLoginFormState -> binding.login.isEnabled = loginState.isDataValid
                is FailedLoginFormState -> {
                    loginState.usernameError?.let { binding.username.error = getString(it) }
                    loginState.passwordError?.let { binding.password.error = getString(it) }
                }
            }
        })
        loginWithPasswordViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success) {
                updateApp(
                    "You successfully signed up using password as: user " +
                            "${SampleAppUser.username} with fake token ${SampleAppUser.fakeToken}"
                )
            }
        })
        binding.username.doAfterTextChanged {
            loginWithPasswordViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.doAfterTextChanged {
            loginWithPasswordViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE ->
                    loginWithPasswordViewModel.login(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                    )
            }
            false
        }
        binding.login.setOnClickListener {
            loginWithPasswordViewModel.login(
                binding.username.text.toString(),
                binding.login.text.toString()
            )
        }
        Log.d(TAG, "Username ${SampleAppUser.username}; fake token ${SampleAppUser.fakeToken}")
    }

    private fun updateApp(successMsg: String) {
        binding.success.text = successMsg
    }
}