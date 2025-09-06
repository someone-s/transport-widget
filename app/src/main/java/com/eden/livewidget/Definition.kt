package com.eden.livewidget

import com.eden.livewidget.data.Provider


enum class Agency(
    val agencyName: Int,
    val agencyDescription: Int,
    val apiProvider: Provider,
) {
    TFL(
        agencyName = R.string.agency_tfl_title,
        agencyDescription = R.string.agency_tfl_about,
        apiProvider = Provider.TFL
    )
}