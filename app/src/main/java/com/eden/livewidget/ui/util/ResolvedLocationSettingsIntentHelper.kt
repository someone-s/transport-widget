package com.eden.livewidget.ui.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings

fun getResolvedLocationSettingsIntent(packageManager: PackageManager): Intent {
    val locationSettingsIntent = Intent().apply {
        action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val locationComponentName =
        locationSettingsIntent.resolveActivity(packageManager)

    return locationSettingsIntent.apply {
        component = locationComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}