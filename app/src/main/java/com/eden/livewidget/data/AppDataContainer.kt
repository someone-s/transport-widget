package com.eden.livewidget.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.eden.livewidget.R
import com.eden.livewidget.data.common.Provider
import com.eden.livewidget.data.rdg.RdgProvider
import com.eden.livewidget.data.tfl.TflProvider
import com.eden.livewidget.data.trasitous.TransitousProvider
import kotlin.reflect.KClass

class AppDataContainer(context: Context): AppContainer {

    override val agencies: List<Agency> = listOf(
        Agency(
            id = "T00",
            agencyName = R.string.agency_t00_title,
            agencyShortDescription = R.string.agency_t00_about_short,
            agencyDescription = R.string.agency_t00_about_full,
            agencyHelp = "https://someone-s.github.io/transport-widget/provider/t00".toUri(),
            apiProviderConstructor = { TransitousProvider(context) },
        ),
        Agency(
            id = "TFL",
            agencyName = R.string.agency_tfl_title,
            agencyShortDescription = R.string.agency_tfl_about_short,
            agencyDescription = R.string.agency_tfl_about_full,
            agencyHelp = "https://someone-s.github.io/transport-widget/provider/tfl".toUri(),
            apiProviderConstructor = { TflProvider(context) },
        ),
        Agency(
            id = "RDG",
            agencyName = R.string.agency_rdg_title,
            agencyShortDescription = R.string.agency_rdg_about_short,
            agencyDescription = R.string.agency_rdg_about_full,
            agencyHelp = "https://someone-s.github.io/transport-widget/provider/rdg".toUri(),
            apiProviderConstructor = { RdgProvider(context) },
        ),
    )

    override fun agencyFromId(id: String): Agency? {
        return agencies.firstOrNull { it.id == id }
    }
}