package cn.ling.yu.permission.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 权限请求模型
 * @author Yu L.
 */
@Parcelize
data class PermissionRequestBean(var permissionList: ArrayList<String>?,var remindRequestCode: Int=200,var requestCode:Int=201,var desc:String?) :
    Parcelable