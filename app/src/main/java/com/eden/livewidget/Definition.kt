package com.eden.livewidget

import com.eden.livewidget.data.Provider


enum class Agency(
    val agencyName: Int,
    val agencyShortDescription: Int,
    val agencyDescription: Int,
    val apiProvider: Provider,
) {
    TFL(
        agencyName = R.string.agency_tfl_title,
        agencyShortDescription = R.string.agency_tfl_about_short,
        agencyDescription = R.string.agency_tfl_about_full,
        apiProvider = Provider.TFL
    )
}