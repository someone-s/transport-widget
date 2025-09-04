package com.eden.livewidget.api

import com.eden.livewidget.data.ArrivalModel
import com.eden.livewidget.data.LivePointApi

class LivePointTflApi(
    private val stopPointId: String
): LivePointApi {
    override fun fetchLatestArrivals(): List<ArrivalModel> {
        return emptyList()
    }
}