package cn.ling.yu.permission

/**
 * 权限申请接口
 * @author Yu L.
 */
interface PermissionAuthorListener {

    fun allGrantPermissions(permissions:ArrayList<String>?)//全部通过
    fun allDeniedPermissions(permissions: ArrayList<String>?)//全部拒绝
    fun againPermissions()//打开系统设置，设置权限
    fun someDeniedPermissions(askPermissions: ArrayList<String>?, deniedPermissions:ArrayList<String>?)//一些需要询问
}