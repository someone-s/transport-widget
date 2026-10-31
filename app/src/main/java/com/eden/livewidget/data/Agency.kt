package com.eden.livewidget.data

import android.net.Uri
import com.eden.livewidget.data.common.Provider

data class Agency(
    val id: String,
    val agencyName: Int,
    val agencyShortDescription: Int,
    val agencyDescription: Int,
    val agencyHelp: Uri,
    private val apiProviderConstructor: () -> Provider,
) {
    val provider: Provider by lazy { apiProviderConstructor() }
}