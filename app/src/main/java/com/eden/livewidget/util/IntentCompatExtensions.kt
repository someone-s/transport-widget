package com.eden.livewidget.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.io.Serializable

inline fun <reified T: Serializable?> Intent.getSerializableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getSerializableExtra(key, T::class.java)
    else
        @Suppress("DEPRECATION")
        getSerializableExtra(key) as? T
}

inline fun <reified T: Serializable?> Bundle.getSerializableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getSerializable(key, T::class.java)
    else
        @Suppress("DEPRECATION")
        getSerializable(key) as? T
}