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

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import cn.com.net.testnav.databinding.ActivityEnableBiometricLoginBinding
import cn.com.net.testnav.ui.WebActivity
import cn.ling.yu.biometricsdk.BiometricCipherResultCallBack
import cn.ling.yu.biometricsdk.BiometricDialogUtils
import cn.ling.yu.permission.PermissionAuthorListener
import cn.ling.yu.permission.PermissionRequestEngine
import cn.ling.yu.permission.bean.PermissionRequestBean
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
                requestSysPermision()
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

    private fun requestSysPermision(){
        val permissions= arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA)
        val permissionRequestBean=PermissionRequestBean(permissions,200,201,null)
        PermissionRequestEngine.with(this)
            .requestPermission(permissionRequestBean)
            .setPermissionAuthorListener(object :PermissionAuthorListener{
                override fun allGrantPermissions(permissions: ArrayList<String>?) {
                    val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun remindPermissions(permissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView, "到系统设置打开权限${permissions.toString()}",Snackbar.LENGTH_LONG).show()
                    requestAgainPermission(permissions)
                }

                override fun againPermissions() {
                    Snackbar.make(window.decorView,"restart",Snackbar.LENGTH_LONG).show()
                }

                override fun someDeniedPermissions(
                    askPermissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView,"some"+askPermissions.toString(),Snackbar.LENGTH_LONG).show()
                    requestAskPermission(askPermissions)
                }

            })
    }

    private fun requestAskPermission(permissions:ArrayList<String>?){
        val permissionRequestBean=PermissionRequestBean(permissions,201,202,null)
        PermissionRequestEngine.with(this)
            .requestAskPermission(permissionRequestBean)
            .setPermissionAuthorListener(object :PermissionAuthorListener{
                override fun allGrantPermissions(permissions: ArrayList<String>?) {
                    val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun remindPermissions(permissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView, "到系统设置打开权限ask${permissions.toString()}",Snackbar.LENGTH_LONG).show()
                }

                override fun againPermissions() {
                    Snackbar.make(window.decorView,"restartAsk",Snackbar.LENGTH_LONG).show()
                }

                override fun someDeniedPermissions(
                    askPermissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView,"someAsk"+askPermissions.toString(),Snackbar.LENGTH_LONG).show()
                }

            })
    }

    private fun requestAgainPermission(permissions: ArrayList<String>?){
        val permissionRequestBean=PermissionRequestBean(permissions,203,204,null)
        PermissionRequestEngine.with(this)
            .requestOpenPermission(permissionRequestBean)
            .setPermissionAuthorListener(object :PermissionAuthorListener{
                override fun allGrantPermissions(permissions: ArrayList<String>?) {
                    val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun remindPermissions(permissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView, "到系统设置打开权限ask2${permissions.toString()}",Snackbar.LENGTH_LONG).show()
                }

                override fun againPermissions() {
                    Snackbar.make(window.decorView,"restartAsk288",Snackbar.LENGTH_LONG).setAction("确定"){
                        requestRestartPermission(permissions)
                    }.show()
                }

                override fun someDeniedPermissions(
                    askPermissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView,"someAsk2"+askPermissions.toString(),Snackbar.LENGTH_LONG).show()
                }

            })

    }

    private fun requestRestartPermission(permissions: ArrayList<String>?){
        val permissionRequestBean=PermissionRequestBean(permissions,205,206,null)
        PermissionRequestEngine.with(this)
            .queryPermissionAuthor(permissionRequestBean)
            .setPermissionAuthorListener(object :PermissionAuthorListener{
                override fun allGrantPermissions(permissions: ArrayList<String>?) {
                    val intent=Intent(this@EnableBiometricLoginActivity,WebActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun remindPermissions(permissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView, "到系统设置打开权限ask3${permissions.toString()}",Snackbar.LENGTH_LONG).show()
                }

                override fun againPermissions() {
                    Snackbar.make(window.decorView,"restartAsk3",Snackbar.LENGTH_LONG).show()
                }

                override fun someDeniedPermissions(
                    askPermissions: ArrayList<String>?) {
                    Snackbar.make(window.decorView,"someAsk3"+askPermissions.toString(),Snackbar.LENGTH_LONG).show()
                }

            })

    }
}