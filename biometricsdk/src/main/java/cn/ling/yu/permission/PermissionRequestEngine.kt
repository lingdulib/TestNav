package cn.ling.yu.permission

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.ling.yu.permission.bean.PermissionRequestBean

/**
 * 权限请求 引擎
 * @author Yu L.
 */
object PermissionRequestEngine {

    private var mPermissionAuthorListener: PermissionAuthorListener? = null
    private lateinit var mContext: Context

    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PERMISSION_ALL_GRANT_RESULTS -> {
                    val permissions = intent?.extras?.getStringArrayList(PERMISSION_ASK_LIST_RETURN)
                    mPermissionAuthorListener?.allGrantPermissions(permissions)
                }
                PERMISSION_SOME_DENIED_RESULTS -> {
                    val permissions = intent?.extras?.getStringArrayList(PERMISSION_ASK_LIST_RETURN)
                    val deniedPermissions = intent?.extras?.getStringArrayList(
                        PERMISSION_REMIND_DENIED_RETURN
                    )
                    mPermissionAuthorListener?.someDeniedPermissions(permissions,deniedPermissions)
                }
                PERMISSION_ALL_DENIED_RESULTS -> {
                    val permissions = intent?.extras?.getStringArrayList(
                        PERMISSION_REMIND_DENIED_RETURN
                    )
                    mPermissionAuthorListener?.allDeniedPermissions(permissions)
                }
                PERMISSION_AGAIN_NOT_REMIND -> {
                    mPermissionAuthorListener?.againPermissions()
                }
            }
        }
    }

    internal fun registerPermissionEngine(context: Context) {
        val intentFilter = IntentFilter()
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        intentFilter.addAction(PERMISSION_ALL_GRANT_RESULTS)
        intentFilter.addAction(PERMISSION_SOME_DENIED_RESULTS)
        intentFilter.addAction(PERMISSION_ALL_DENIED_RESULTS)
        intentFilter.addAction(PERMISSION_AGAIN_NOT_REMIND)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(mBroadcastReceiver, intentFilter)
    }

    internal fun unRegisterPermissionEngine(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver)
    }

    internal fun sendPermissionEngine(
        context: Context,
        action: String,
        permissions: ArrayList<String>?,
        remindPermissions: ArrayList<String>?
    ) {
        val intent = Intent()
        intent.apply {
            val bundle = Bundle()
            bundle.putStringArrayList(PERMISSION_ASK_LIST_RETURN, permissions)
            bundle.putStringArrayList(PERMISSION_REMIND_DENIED_RETURN, remindPermissions)
            putExtras(bundle)
            addCategory(Intent.CATEGORY_DEFAULT)
            setAction(action)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun setPermissionAuthorListener(permissionAuthorListener: PermissionAuthorListener?) {
        mPermissionAuthorListener = permissionAuthorListener
    }

    fun with(context: Context):PermissionRequestEngine{
        mContext=context
        return this
    }

    fun requestPermission(permissionRequestBean: PermissionRequestBean){
        if(this::mContext.isInitialized){
            val intent=Intent(mContext,PermissionActivity::class.java)
            intent.apply {
                putExtra(PERMISSION_REQUEST_OBJ,permissionRequestBean)
                putExtra(PERMISSION_REQUEST_TYPE,PERMISSION_REQUEST)
            }
            mContext.startActivity(intent)
        }
    }

    //询问请求
    fun requestAskPermission(permissionRequestBean: PermissionRequestBean){
        if(this::mContext.isInitialized){
            val intent=Intent(mContext,PermissionActivity::class.java)
            intent.apply {
                putExtra(PERMISSION_REQUEST_OBJ,permissionRequestBean)
                putExtra(PERMISSION_REQUEST_TYPE, PERMISSION_ASK_REQUEST)
            }
            mContext.startActivity(intent)
        }
    }

    //打开系统设置,申请权限
    fun requestOpenPermission(permissionRequestBean: PermissionRequestBean){
        if(this::mContext.isInitialized){
            val intent=Intent(mContext,PermissionActivity::class.java)
            intent.apply {
                putExtra(PERMISSION_REQUEST_OBJ,permissionRequestBean)
                putExtra(PERMISSION_REQUEST_TYPE, PERMISSION_ALL_DENIED_REQUEST)
            }
            mContext.startActivity(intent)
        }
    }
}