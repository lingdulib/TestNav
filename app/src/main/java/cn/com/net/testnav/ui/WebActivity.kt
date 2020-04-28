package cn.com.net.testnav.ui

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import cn.com.net.testnav.databinding.ActivityWebBinding
import cn.ling.yu.permission.*
import com.google.android.material.snackbar.Snackbar


/**
 * webview
 * @date 2020/4/24
 * @author Yu L.
 *
 */
class WebActivity : AppCompatActivity(),ActivityCompat.OnRequestPermissionsResultCallback{

    private val TAG = WebActivity::class.java.simpleName
    private lateinit var binding:ActivityWebBinding
    private var mFilePathCallback:ValueCallback<Array<Uri>>?=null
    companion object{
        private const val RC_CAMERA_PERM = 123
        private const val RC_STORE_LOCATION=124
        private const val PERMISSION_REQUEST_CAMERA = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val webPackageInfo = WebViewCompat.getCurrentWebViewPackage(this)
        val versionName = webPackageInfo?.versionName
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
        val permissions=ArrayList<String>()
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        val denPermissions=obtainPermissionDeniedResultList(permissions).size
        if (denPermissions==0) {
            loadUrl()
        } else {
            requestCameraPermission()
        }
    }


    private fun loadUrl(){
        binding.actWeb.loadUrl("https://www.baidu.com")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1000 && resultCode== Activity.RESULT_OK){
            val urls=WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            Log.e(TAG,"urls:$urls")
            mFilePathCallback?.onReceiveValue(urls)
            mFilePathCallback = null
        }else if(requestCode==200){
            Handler().postDelayed({
                if(Settings.canDrawOverlays(this)){//需要重启ui,检测设置中打开权限

                }else{

                }
            },500)
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (obtainAllPermissionGrantResult(grantResults)) {
                Snackbar.make(window.decorView,"权限以获得",Snackbar.LENGTH_LONG).show()
                loadUrl()
            } else {
                // Permission request was denied.
                if(!shouldShowRequestPermissionRationaleCompat(Manifest.permission.ACCESS_FINE_LOCATION)){
                    showAppSettingDetail(200)
                    Snackbar.make(window.decorView,"权限被拒绝，并勾选不再提示。",Snackbar.LENGTH_LONG).show()
                }else{
                    Snackbar.make(window.decorView,"下次请求向用户解释",Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun requestCameraPermission() {
        // Permission has not been granted and must be requested.
        //曾经被拒绝，并未勾选不在提示时返回true
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(window.decorView,"解释权限是做什么用的",Snackbar.LENGTH_LONG).show()
            val permissions=ArrayList<String>()
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.CAMERA)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissionsCompat(permissions, PERMISSION_REQUEST_CAMERA)
        } else {
            val permissions=ArrayList<String>()
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.CAMERA)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissionsCompat(permissions, PERMISSION_REQUEST_CAMERA)
        }
    }

}