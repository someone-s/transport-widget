// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.arrivals

import android.content.Context
import com.eden.livewidget.data.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.eden.livewidget.data.arrivals.ArrivalsDataSource.Companion.FetchResult

class ArrivalsRepository(
    private val arrivalsDataSource: ArrivalsDataSource,
) {

    private val latestArrivalsMutable = MutableStateFlow(Pair(DataValidity.INVALID_UNINITIALIZED, emptyList<ArrivalModel>()))
    val latestArrivals = latestArrivalsMutable.asStateFlow()

    suspend fun fetchLatestArrival(context: Context) {
        latestArrivalsMutable.update {
            val output = arrivalsDataSource.fetchLatestArrivals(context)

            Pair(
                first =
                    when (output.first) {
                        FetchResult.SUCCESS -> DataValidity.VALID
                        FetchResult.ERROR_UNREACHABLE -> DataValidity.INVALID_UNREACHABLE
                        FetchResult.ERROR_AUTHENTICATION -> DataValidity.INVALID_AUTHENTICATION
                    },
                second = output.second
            )
        }
    }

    companion object {

        enum class DataValidity { VALID, INVALID_AUTHENTICATION, INVALID_UNREACHABLE, INVALID_UNINITIALIZED }

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