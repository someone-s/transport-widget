// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.arrivals

import android.content.Context
import com.eden.livewidget.data.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

class ArrivalsRepository(
    private val arrivalsDataSource: ArrivalsDataSource,
) {

    private val arrivalsDataMutable = MutableStateFlow(ArrivalsData())
    val arrivalsData = arrivalsDataMutable.asStateFlow()

    suspend fun fetchLatestArrival(context: Context): FetchResult {
        val output = arrivalsDataSource.fetchLatestArrivals(context)

        arrivalsDataMutable.getAndUpdate { previousData ->
            when (output.first) {
                ArrivalsDataSource.FetchResult.SUCCESS ->
                    ArrivalsData(ArrivalsData.Validity.VALID, output.second)
                ArrivalsDataSource.FetchResult.ERROR_UNRESOLVED ->
                    ArrivalsData(ArrivalsData.Validity.INVALID, previousData.lastValidData)
                ArrivalsDataSource.FetchResult.ERROR_UNREACHABLE ->
                    ArrivalsData(ArrivalsData.Validity.INVALID, previousData.lastValidData)
                ArrivalsDataSource.FetchResult.ERROR_AUTHENTICATION ->
                    ArrivalsData(ArrivalsData.Validity.INVALID, previousData.lastValidData)
            }
        }

        return when (output.first) {
            ArrivalsDataSource.FetchResult.SUCCESS -> FetchResult.SUCCESS
            ArrivalsDataSource.FetchResult.ERROR_UNRESOLVED -> FetchResult.ERROR_UNRESOLVED
            ArrivalsDataSource.FetchResult.ERROR_UNREACHABLE -> FetchResult.ERROR_UNREACHABLE
            ArrivalsDataSource.FetchResult.ERROR_AUTHENTICATION -> FetchResult.ERROR_AUTHENTICATION
        }
    }

    enum class FetchResult {
        SUCCESS,
        ERROR_UNRESOLVED,
        ERROR_UNREACHABLE,
        ERROR_AUTHENTICATION,
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