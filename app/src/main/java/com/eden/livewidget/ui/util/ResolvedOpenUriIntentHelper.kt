package com.eden.livewidget.ui.util

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent

fun getResolvedOpenUriIntent(uri: Uri): Pair<Intent, Bundle?> {

    val customTabs = CustomTabsIntent.Builder().build().also {
        it.intent.apply {
            data = uri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
    }

    return Pair(customTabs.intent, customTabs.startAnimationBundle)
}
