// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


class LivePointDataSource(
    private val livePointApi: LivePointApi,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun fetchLatestArrivals(): List<ArrivalModel> =
    // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            livePointApi.fetchLatestArrivals()
        }
}

interface LivePointApi {
    fun fetchLatestArrivals(): List<ArrivalModel>
}