package com.eden.livewidget.data

import android.net.Uri
import com.eden.livewidget.data.common.Provider
import com.eden.livewidget.ui.Agency

data class Agency(
    val agencyName: Int,
    val agencyShortDescription: Int,
    val agencyDescription: Int,
    val agencyHelp: Uri,
    private val apiProviderConstructor: () -> Provider,
) {
    val apiProvider: Provider by lazy { apiProviderConstructor() }

    fun agencyToString(agency: Agency): String = agency.name

    fun agencyFromString(string: String?): Agency? {

        if (string == null)
            return null

        var agency: com.eden.livewidget.ui.Agency
        try {
            agency = Agency.valueOf(string)
        } catch (_: IllegalArgumentException) {
            return null
        }

        return agency
    }
}