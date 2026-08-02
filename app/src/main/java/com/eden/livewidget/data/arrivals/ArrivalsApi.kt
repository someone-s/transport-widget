package com.eden.livewidget.data.arrivals

import android.content.Context


interface ArrivalsApi {
    suspend fun fetchLatestArrivals(context: Context): List<ArrivalModel>
}


