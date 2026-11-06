package com.eden.livewidget.data

import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.data.location.repository.LocationRepository
import com.eden.livewidget.data.widget.repository.WidgetRepository

interface AppContainer {
    val agencies: List<Agency>

    fun agencyFromId(id: String): Agency?

    val widgetRepository: WidgetRepository

    val locationRepository: LocationRepository
}