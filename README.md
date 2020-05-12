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



