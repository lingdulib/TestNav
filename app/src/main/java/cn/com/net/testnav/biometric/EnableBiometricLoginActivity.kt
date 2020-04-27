/*
 * Copyright (C) 2020 The Android Open Source Project
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
 * limitations under the License
 */
package cn.com.net.testnav.biometric

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import cn.com.net.testnav.databinding.ActivityEnableBiometricLoginBinding
import cn.com.net.testnav.ui.WebActivity
import cn.ling.yu.biometricsdk.BiometricCipherResultCallBack
import cn.ling.yu.biometricsdk.BiometricDialogUtils
import com.google.android.material.snackbar.Snackbar
import javax.crypto.Cipher

class EnableBiometricLoginActivity : AppCompatActivity() {
    private val TAG = "EnableBiometricLogin"
    private val loginViewModel by viewModels<LoginWithPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEnableBiometricLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cancel.setOnClickListener {
            val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginViewModel.loginWithPasswordFormState.observe(this, Observer { formState ->
            val loginState = formState ?: return@Observer
            when (loginState) {
                is SuccessfulLoginFormState -> binding.authorize.isEnabled = loginState.isDataValid
                is FailedLoginFormState -> {
                    loginState.usernameError?.let { binding.username.error = getString(it) }
                    loginState.passwordError?.let { binding.password.error = getString(it) }
                }
            }
        })
        loginViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success) {
                showBiometricPromptForEncryption()
            }
        })
        binding.username.doAfterTextChanged {
            loginViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.doAfterTextChanged {
            loginViewModel.onLoginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }
        binding.password.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE ->
                    loginViewModel.login(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                    )
            }
            false
        }
        binding.authorize.setOnClickListener {
            loginViewModel.login(binding.username.text.toString(), binding.password.text.toString())
        }
    }

    private fun showBiometricPromptForEncryption() {
        BiometricDialogUtils.setBiometricCipherResultCallBack(object:BiometricCipherResultCallBack{
            override fun obtainCipher(cipher: Cipher) {
                BiometricDialogUtils.saveCiphertextWrapper("",cipher,this@EnableBiometricLoginActivity)
                val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun obtainDCipher(cipher: Cipher) {

            }

            override fun obtainErrorInfo(errCode: Int, errorMsg: String?) {
                Snackbar.make(window.decorView,"$errCode==$errorMsg",Snackbar.LENGTH_LONG).setAction("确定",null).show()
            }
        })
        BiometricDialogUtils.showBiometricPromptForEncryption(null,this,false,::failResultCallBack)
    }

    private fun failResultCallBack(code:Int,error:String?){
        Snackbar.make(window.decorView,error?:"认证失败.",Snackbar.LENGTH_INDEFINITE).setAction("确定",null).show()
    }
}