package com.example.smartlab.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

object PermissionHelper {
    
    fun hasSystemAlertWindowPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestSystemAlertWindowPermission(activity: Activity, requestCode: Int = 1001) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        activity.startActivityForResult(intent, requestCode)
    }
    
    fun openSystemAlertWindowSettings(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestSystemAlertWindowPermission(activity)
        }
    }
}

