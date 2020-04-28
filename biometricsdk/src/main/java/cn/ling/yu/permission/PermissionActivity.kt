package cn.ling.yu.permission

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.ling.yu.permission.bean.PermissionRequestBean
import com.google.android.material.snackbar.Snackbar

/**
 * 申请权限
 * @author Yu L.
 */
class PermissionActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var mPermissionRequestBean: PermissionRequestBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPermission()
    }

    private fun loadPermission() {
        mPermissionRequestBean = intent.getParcelableExtra(PERMISSION_REQUEST_OBJ)
        val permissionRequestType = intent.getStringExtra(PERMISSION_REQUEST_TYPE)
        if (mPermissionRequestBean == null) {
            Snackbar.make(
                window.decorView,
                "PermissionRequestBean object is NULL.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(
                "确定"
            ) { finish() }
            return
        }
        PermissionRequestEngine.registerPermissionEngine(this)
        val permissionArrs = mPermissionRequestBean?.permissionList
        when(permissionRequestType){
            PERMISSION_REQUEST->{
                if (permissionArrs != null && permissionArrs.isNotEmpty()) {
                    if (obtainPermissionDeniedResultList(permissionArrs).isEmpty()) {
                        PermissionRequestEngine.sendPermissionEngine(
                            this,
                            PERMISSION_ALL_GRANT_RESULTS,
                            mPermissionRequestBean!!.permissionList,
                            null
                        )
                        finish()
                    } else {
                        requestPermission(permissionArrs)
                    }
                } else {
                    Snackbar.make(
                        window.decorView,
                        "PermissionRequestBean object [permissionList or permission] is NULL.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "确定"
                    ) { unRegisterEngine() }
                    return
                }
            }
            PERMISSION_ASK_REQUEST->{
               AlertDialog.Builder(this).setTitle("权限申请")
                    .setMessage(mPermissionRequestBean!!.desc?:"使用此项功能，应用需要此项权限.")
                    .setPositiveButton("确定"
                    ) { p0, _ -> if(permissionArrs!=null && permissionArrs.isNotEmpty()){
                        p0.dismiss()
                        requestPermission(permissionArrs)
                    } }.setNegativeButton("取消"){p0,_->
                        run {
                            p0.dismiss()
                            unRegisterEngine()
                        }
                    }.create().show()
            }
            PERMISSION_ALL_DENIED_REQUEST->{
                AlertDialog.Builder(this).setTitle("权限申请")
                    .setMessage(mPermissionRequestBean!!.desc?:"使用此项功能，应用需要此项权限.")
                    .setPositiveButton("去设置"
                    ) { p0, _ -> if(permissionArrs!=null && permissionArrs.isNotEmpty()){
                        p0.dismiss()
                        showAppSettingDetail(mPermissionRequestBean!!.remindRequestCode)
                    } }.setNegativeButton("取消"){p0,_->
                        run {
                            p0.dismiss()
                            unRegisterEngine()
                        }
                    }.create().show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mPermissionRequestBean!!.remindRequestCode) {
            Handler().postDelayed({
                PermissionRequestEngine.sendPermissionEngine(
                    this,
                    PERMISSION_AGAIN_NOT_REMIND,
                    null,
                    null
                )
                finish()
            }, 500)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == mPermissionRequestBean!!.requestCode) {
            if (obtainAllPermissionGrantResult(grantResults)) {
                PermissionRequestEngine.sendPermissionEngine(
                    this,
                    PERMISSION_ALL_GRANT_RESULTS,
                    mPermissionRequestBean!!.permissionList,
                    null
                )
                finish()
            } else {
                val deniedList =
                    obtainPermissionDeniedResultList(mPermissionRequestBean!!.permissionList)
                val askPermissionList = obtainPermissionAskDeniedRequest(deniedList)
                val remindPermissionList = obtainPermissionRemindDeniedRequest(deniedList)
                if (askPermissionList.isNotEmpty()) {
                    PermissionRequestEngine.sendPermissionEngine(
                        this,
                        PERMISSION_SOME_DENIED_RESULTS,
                        askPermissionList,
                        remindPermissionList
                    )
                    finish()
                } else {
                    PermissionRequestEngine.sendPermissionEngine(
                        this,
                        PERMISSION_ALL_DENIED_RESULTS,
                        null,
                        deniedList
                    )
                    finish()
                }
            }
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            unRegisterEngine()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun unRegisterEngine() {
        PermissionRequestEngine.unRegisterPermissionEngine(this)
        finish()
    }

    private fun requestPermission(permissions: ArrayList<String>) {
        requestPermissionsCompat(permissions, mPermissionRequestBean!!.requestCode)
    }
}