package com.eden.livewidget.widget.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

fun getResolvedNetworkSettingsIntent(packageManager: PackageManager): Intent {

    val networkSettingsIntent = Intent().apply {

        action =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                Settings.ACTION_DATA_USAGE_SETTINGS
            else
                Settings.ACTION_SETTINGS

    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val networkSettingsComponentName =
        networkSettingsIntent.resolveActivity(packageManager)

    return networkSettingsIntent.apply {
        component = networkSettingsComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}