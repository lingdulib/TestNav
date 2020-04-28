package cn.ling.yu.permission

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(permissionsArray: ArrayList<String>,
                                               requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissionsArray.toTypedArray(), requestCode)
}

fun AppCompatActivity.obtainAllPermissionGrantResult(grantResults: IntArray?):Boolean{
    var flag =true
    if(grantResults!=null && grantResults.isNotEmpty()){
        for(grantResult in grantResults){
            if(grantResult!= PackageManager.PERMISSION_GRANTED){
                flag=false
                break
        }
        }
    }else{
        flag=false
    }
    return flag
}

fun AppCompatActivity.obtainPermissionDeniedResultList(permissionsArray: ArrayList<out String>?):ArrayList<String>{
     val deniedList= ArrayList<String>()
     if(permissionsArray==null || permissionsArray.isEmpty()){
         return deniedList
     }
     for(permissionStr in permissionsArray){
         if(checkCallingOrSelfPermission(permissionStr)==PackageManager.PERMISSION_DENIED){
                deniedList.add(permissionStr)
         }
     }
     return deniedList
}

//曾经被拒绝，并未勾选不再提示时返回true
fun AppCompatActivity.obtainPermissionAskDeniedRequest(permissionsArray: ArrayList<String>?):ArrayList<String>{
    val askDeniedList= arrayListOf<String>()
    if(permissionsArray==null || permissionsArray.isEmpty()){
        return askDeniedList
    }
    for(permission in permissionsArray){
        if(shouldShowRequestPermissionRationaleCompat(permission)){
            askDeniedList.add(permission)
        }
    }
    return askDeniedList
}

//拒绝，并勾选不再提示
fun AppCompatActivity.obtainPermissionRemindDeniedRequest(permissionsArray: ArrayList<String>?):ArrayList<String>{
    val remindDeniedList= arrayListOf<String>()
    if(permissionsArray==null || permissionsArray.isEmpty()){
        return remindDeniedList
    }
    for(permission in permissionsArray){
        if(!shouldShowRequestPermissionRationaleCompat(permission)){
            remindDeniedList.add(permission)
        }
    }
    return remindDeniedList
}

fun AppCompatActivity.showAppSettingDetail(requestCode:Int){
    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package",packageName,null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivityForResult(intent,requestCode)
}

