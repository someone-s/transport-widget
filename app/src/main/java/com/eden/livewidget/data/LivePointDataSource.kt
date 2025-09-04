// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class LivePointDataSource(
    private val livePointApi: LivePointApi,
    private val refreshIntervalMS: Long = 5000
) {
    val latestArrivals: Flow<List<ArrivalModel>> = flow {
        // run as long as context visible
        while (true) {
            val lastestArrival = livePointApi.fetchLatestArrivals()
            emit(lastestArrival)
            delay(refreshIntervalMS)
        }
    }

}

interface LivePointApi {
    fun fetchLatestArrivals(): List<ArrivalModel>
}