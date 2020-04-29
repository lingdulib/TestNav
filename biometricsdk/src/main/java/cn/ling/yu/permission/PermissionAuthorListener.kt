package cn.ling.yu.permission

/**
 * 权限申请接口
 * @author Yu L.
 */
interface PermissionAuthorListener {

    fun allGrantPermissions(permissions:ArrayList<String>?)//全部通过
    fun againPermissions()//需要重启查看权限是否通过
    fun remindPermissions(permissions: ArrayList<String>?)//打开系统设置，设置权限
    fun someDeniedPermissions(askPermissions: ArrayList<String>?)//一些需要询问
}