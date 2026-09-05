package com.eden.livewidget.widget.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

fun getResolvedBatterySaverSettingsIntent(packageManager: PackageManager): Intent {
    val batterySaverSettingsIntent = Intent().apply {
        action = Settings.ACTION_BATTERY_SAVER_SETTINGS
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val batterySaverComponentName =
        batterySaverSettingsIntent.resolveActivity(packageManager)

    return batterySaverSettingsIntent.apply {
        component = batterySaverComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}