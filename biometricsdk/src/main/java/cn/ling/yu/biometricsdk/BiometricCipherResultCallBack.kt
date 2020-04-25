package cn.ling.yu.biometricsdk

import javax.crypto.Cipher

/**
 * @date 2020/4/24
 * @author Yu L.
 *
 */
interface BiometricCipherResultCallBack {
    fun obtainCipher(cipher: Cipher)
    fun obtainDCipher(cipher: Cipher)
    fun obtainErrorInfo(errCode:Int,errorMsg:String?)
}