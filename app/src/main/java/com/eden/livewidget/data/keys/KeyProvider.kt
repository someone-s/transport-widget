package com.eden.livewidget.data.keys

import kotlinx.coroutines.flow.SharedFlow

interface KeyProvider {
    val key: SharedFlow<String?>

    fun refreshKey(): String

    fun setKey(newKey: String)
}