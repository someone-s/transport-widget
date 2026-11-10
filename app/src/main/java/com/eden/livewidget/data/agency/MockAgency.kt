package com.eden.livewidget.data.agency

import androidx.core.net.toUri
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.common.mockProvider

val mockAgency = Agency(
    id = "___",
    agencyName = R.string.agency_t00_title,
    agencyShortDescription = R.string.agency_t00_about_short,
    agencyDescription = R.string.agency_t00_about_full,
    agencyHelp = "".toUri(),
    apiProviderConstructor = { mockProvider }
)