package com.eden.livewidget.data

interface AppContainer {
    val agencies: List<Agency>

    fun agencyFromId(id: String): Agency?
}