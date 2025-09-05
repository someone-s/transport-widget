// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.arrivals

import com.eden.livewidget.data.utils.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ArrivalsRepository(
    private val arrivalsDataSource: ArrivalsDataSource,
) {
    private val latestArrivalsMutable = MutableStateFlow(emptyList<ArrivalModel>())
    val latestArrivals = latestArrivalsMutable.asStateFlow()

    suspend fun fetchLatestArrival() {
        latestArrivalsMutable.update { arrivalsDataSource.fetchLatestArrivals() }

    }

    companion object {

        data class ArrivalsKey(
            val apiProvider: Provider,
            val apiValue: String,
        )
        private var instances: MutableMap<ArrivalsKey, ArrivalsRepository> = mutableMapOf()

        fun getInstance(apiProvider: Provider, apiValue: String): ArrivalsRepository {
            val key = ArrivalsKey(apiProvider, apiValue)
            if (!instances.contains(key)) {
                instances[key] = ArrivalsRepository(
                    ArrivalsDataSource(
                        apiProvider.arrivalsApiConstructor(apiValue),
                        Dispatchers.IO
                    ),
                )
            }

            return instances[key] as ArrivalsRepository
        }
    }
}