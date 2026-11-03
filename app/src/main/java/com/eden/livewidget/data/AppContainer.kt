package com.eden.livewidget.data

import com.eden.livewidget.data.agency.Agency

interface AppContainer {
    val agencies: List<Agency>

    fun agencyFromId(id: String): Agency?
}