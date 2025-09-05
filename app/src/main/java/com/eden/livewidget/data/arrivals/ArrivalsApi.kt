package com.eden.livewidget.data.arrivals


interface ArrivalsApi {
    fun fetchLatestArrivals(): List<ArrivalModel>
}


