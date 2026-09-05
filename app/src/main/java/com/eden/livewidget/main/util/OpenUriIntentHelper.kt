package com.eden.livewidget.main.util

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

fun getOpenUriIntent(uri: Uri): Intent {
    return CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .build()
        .intent
        .apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
}