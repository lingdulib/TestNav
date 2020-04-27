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

fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>,
                                               requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
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

fun AppCompatActivity.obtainPermissionDeniedResultList(permissionsArray: Array<out String>):List<String>{
     val deniedList= arrayListOf<String>()
     for(permissionStr in permissionsArray){
         if(checkCallingOrSelfPermission(permissionStr)==PackageManager.PERMISSION_DENIED){
                deniedList.add(permissionStr)
         }
     }
     return deniedList
}

fun AppCompatActivity.showAppSettingDetail(requestCode:Int){
    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package",packageName,null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivityForResult(intent,requestCode)
}

