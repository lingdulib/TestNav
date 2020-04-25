package cn.com.net.testnav.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import cn.com.net.testnav.databinding.ActivityWebBinding

/**
 * webview
 * @date 2020/4/24
 * @author Yu L.
 *
 */
class WebActivity : AppCompatActivity() {

    private val TAG = WebActivity::class.java.simpleName
    private lateinit var binding:ActivityWebBinding
    private var mFilePathCallback:ValueCallback<Array<Uri>>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val webPackageInfo = WebViewCompat.getCurrentWebViewPackage(this)
        val versionName = webPackageInfo?.versionName
        binding.actWeb.loadUrl("https://www.baidu.com")
        val webChromeClient=object:WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                super.onPermissionRequestCanceled(request)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mFilePathCallback=filePathCallback
                val intent=fileChooserParams?.createIntent()
                intent?.let {
                    startActivityForResult(it,1000)
                }
                return  true
            }
        }
        val webViewClientCompat=object:WebViewClientCompat(){
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
//                if(TextUtils.equals(request.url.host,"www.baidu.com")){
//                    return false;
//                }
//                return super.shouldOverrideUrlLoading(view, request)
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
            }

        }
        binding.actWeb.webChromeClient=webChromeClient
        binding.actWeb.webViewClient=webViewClientCompat
        val setting=binding.actWeb.settings
        setting.allowFileAccess=true
        setting.javaScriptEnabled=true
        setting.mixedContentMode=WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        setting.allowContentAccess=true
        setting.allowFileAccessFromFileURLs=true
        setting.allowUniversalAccessFromFileURLs=true
        setting.useWideViewPort=true
        setting.javaScriptCanOpenWindowsAutomatically=true
        setting.loadWithOverviewMode=true
        setting.domStorageEnabled=true
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            binding.actWeb.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND,true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1000 && resultCode== Activity.RESULT_OK){
            val urls=WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            Log.e(TAG,"urls:$urls")
            mFilePathCallback?.onReceiveValue(urls)
            mFilePathCallback = null
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(binding.actWeb.canGoBack()){
                binding.actWeb.goBack()
            }else{
                finish()
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}