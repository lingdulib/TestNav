# TestNav
android6.0+指纹识别，webview简单使用

> Android 6.0+以上指纹识别
> Webview 简单使用，支持文件选择
> 支持动态权限请求


----------------------------------------------

# android 生物识别sdk

**仅支持Android 6.0+以上生物识别认证**<br />

- 将加密方式传入指纹识别类使用方法



```kotlin
   BiometricDialogUtils.setBiometricCipherResultCallBack(object:BiometricCipherResultCallBack{
            override fun obtainCipher(cipher: Cipher) {
                //将数据加密保存
                BiometricDialogUtils.saveCiphertextWrapper("",cipher,this@EnableBiometricLoginActivity)
            }

            override fun obtainDCipher(cipher: Cipher) {
				//解密数据
            }

            override fun obtainErrorInfo(errCode: Int, errorMsg: String?) {
                //认证结果返回
                Snackbar.make(window.decorView,"$errCode==$errorMsg",Snackbar.LENGTH_LONG).setAction("确定",null).show()
            }
        })
        BiometricDialogUtils.showBiometricPromptForEncryption(null,this,false,::failResultCallBack)
        	
          //验证设备是否有生物识别硬件或者是否可用  
          private fun failResultCallBack(code:Int,error:String?){
        	Snackbar.make(window.decorView,error?:"认证失败.",Snackbar.LENGTH_INDEFINITE).setAction("确定",null).show()
         }
```


- 通过指纹认证解密数据



```kotlin
 private var ciphertextWrapper:CiphertextWrapper?=null
 ciphertextWrapper==BiometricDialogUtils.getCiphertextWrapper(this)
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
         private fun failResultCallBack(code:Int,error:String?){
        Snackbar.make(window.decorView,error?:"认证失败.",Snackbar.LENGTH_LONG).setAction("确定",null).show()
    }
```
> showBiometricPromptForEncryption方法参数解释说明


<br />


| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| ciphertextWrapper | class | 自定义类对象 |
| appCompatActivity | class | Activity |
| decryption | Boolean | true:解密,false:加密 |
| listenerFailer | function | 函数，code:错误码Int,errorMsg:错误信息,String<br /> |

>  可修改的一些字段

| 字段 | 说明 |
| --- | --- |
| prompt_info_title | 指纹弹出对话框，标题 |
| prompt_info_subtitle | 指纹弹出对话框，子标题 |
| prompt_info_description | 指纹弹出对话框，描述 |
| prompt_info_use_app_password | 指纹弹出对话框，按钮文字设置 |



# android权限管理sdk

> 概述

 <br />主要描述了android 6.0+系统动态权限申请方案，动态权限申请主要通过**PermissionRequestEngine**类实现，具体使用如下所示<br />

> PermissionRequestBean 权限参数说明


<br /> 

| 字段 | 类型 | 可选 | 说明 |
| --- | --- | --- | --- |
| _permissionList_ | List<String> | 否 | 权限数组，也可是一个权限 |
| _remindRequestCode_ | int | 是 | 跳转设置授权，请求响应码，默认200 |
| _requestCode_ | int | 是 | 请求权限，请求响应码，默认201 |
| _desc_ | String | 是 | 权限申请描述 |

> PermissionAuthorListener 接口说明



```kotlin
    fun allGrantPermissions(permissions:ArrayList<String>?)//全部通过
    fun againPermissions()//到设置授予权限,查看权限是否通过
    fun remindPermissions(permissions: ArrayList<String>?)//打开系统设置，设置权限
    fun someDeniedPermissions(askPermissions: ArrayList<String>?)//一些需要询问的权限
```


> 可修改的一些描述




| 字段 | 说明 |
| --- | --- |
| permission_info_ask_title | 询问权限时，标题 |
| permission_info_ask_desc | 询问权限时，描述 |
| permission_info_setting_title | 跳转到设置授予权限时，标题 |
| permission_info_setting_desc | 跳转到设置授予权限时，描述 |



- 请求权限示例



```kotlin
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
```

- 首次拒绝权限，并未勾选不再提示选项示例



```kotlin
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

```


- 拒绝权限，并勾选不再提示选项示例

 
```kotlin
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

```


- 跳转系统设置授予权限，并获取授予结果示例

 
```kotlin
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
```

<br />
<br />





