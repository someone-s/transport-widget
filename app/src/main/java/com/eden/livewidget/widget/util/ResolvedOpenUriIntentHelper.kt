package com.eden.livewidget.widget.util

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun getResolvedOpenUriIntent(packageManager: PackageManager, uri: Uri): Intent {

    val openUrlIntent = CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .build()
        .intent
        .apply {
            data = uri
        }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val openUriComponentName =
        openUrlIntent.resolveActivity(packageManager)

    return openUrlIntent.apply {
        component = openUriComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}
