package com.eden.livewidget.widget.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.net.toUri

fun getResolvedMeteredSettingsIntent(packageManager: PackageManager, packageName: String): Intent {
    val meteredSettingsIntent = Intent().apply {
        action = Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS
        data = "package:$packageName".toUri()
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val meteredSettingsComponentName =
        meteredSettingsIntent.resolveActivity(packageManager)

    return meteredSettingsIntent.apply {
        component = meteredSettingsComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}