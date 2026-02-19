package com.maksimowiczm.foodyou.app.ui.personalization

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal actual fun rememberSmartScaleToggle(onToggle: (Boolean) -> Unit): (Boolean) -> Unit {
    val context = LocalContext.current

    val permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            results ->
            if (results.values.all { it }) onToggle(true) else onToggle(false)
        }

    return { newValue ->
        if (!newValue) {
            onToggle(false)
        } else {
            val allGranted =
                permissions.all {
                    ContextCompat.checkSelfPermission(context, it) ==
                        PackageManager.PERMISSION_GRANTED
                }
            if (allGranted) onToggle(true) else launcher.launch(permissions)
        }
    }
}
