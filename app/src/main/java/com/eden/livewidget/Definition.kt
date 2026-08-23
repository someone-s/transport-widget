package com.eden.livewidget

import android.net.Uri
import androidx.core.net.toUri
import com.eden.livewidget.data.Provider


enum class Agency(
    val agencyName: Int,
    val agencyShortDescription: Int,
    val agencyDescription: Int,
    val apiProvider: Provider,
    val agencyHelp: Uri,
) {
    TFL(
        agencyName = R.string.agency_tfl_title,
        agencyShortDescription = R.string.agency_tfl_about_short,
        agencyDescription = R.string.agency_tfl_about_full,
        apiProvider = Provider.TFL,
        agencyHelp = "https://someone-s.github.io/transport-widget/provider/tfl".toUri()
    ),
    RDG(
        agencyName = R.string.agency_rdg_title,
        agencyShortDescription = R.string.agency_rdg_about_short,
        agencyDescription = R.string.agency_rdg_about_full,
        apiProvider = Provider.RDG,
        agencyHelp = "https://someone-s.github.io/transport-widget/provider/rdg".toUri()
    ),
    TOO(
        agencyName = R.string.agency_t00_title,
        agencyShortDescription = R.string.agency_t00_about_short,
        agencyDescription = R.string.agency_t00_about_full,
        apiProvider = Provider.T00,
        agencyHelp = "https://someone-s.github.io/transport-widget/provider/t00".toUri()
    )
}

fun agencyToString(agency: Agency): String = agency.name

fun agencyFromString(string: String?): Agency? {

    if (string == null)
        return null

    var agency: Agency
    try {
        agency = Agency.valueOf(string)
    } catch (_: IllegalArgumentException) {
        return null
    }

    return agency
}