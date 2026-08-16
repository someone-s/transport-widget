package com.eden.livewidget.data.common.keys

import android.content.Context

interface KeyProvider {
    fun getKey(context: Context): String

    fun setKey(context: Context, key: String)
}