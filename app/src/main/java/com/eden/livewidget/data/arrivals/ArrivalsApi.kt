package com.eden.livewidget.data.arrivals

import android.content.Context


interface ArrivalsApi {
    fun fetchLatestArrivals(context: Context): List<ArrivalModel>
}


