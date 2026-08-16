// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.common.arrivals

import android.content.Context
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.filter.State as FilterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.time.LocalDateTime

class Repository(
    private val dataSource: DataSource,
) {

    private val arrivalsDataMutable = MutableStateFlow(Data())
    val arrivalsData = arrivalsDataMutable.asStateFlow()

    suspend fun fetchLatestArrival(context: Context): FetchResult {
        val output = dataSource.fetchLatestArrivals(context)


        arrivalsDataMutable.getAndUpdate { previousData ->
            when (output.first) {
                DataSource.FetchResult.SUCCESS ->
                    Data(LocalDateTime.now(), Data.Validity.VALID, output.second)
                DataSource.FetchResult.ERROR_UNRESOLVED ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
                DataSource.FetchResult.ERROR_UNREACHABLE ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
                DataSource.FetchResult.ERROR_AUTHENTICATION ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
            }
        }

        return when (output.first) {
            DataSource.FetchResult.SUCCESS -> FetchResult.SUCCESS
            DataSource.FetchResult.ERROR_UNRESOLVED -> FetchResult.ERROR_UNRESOLVED
            DataSource.FetchResult.ERROR_UNREACHABLE -> FetchResult.ERROR_UNREACHABLE
            DataSource.FetchResult.ERROR_AUTHENTICATION -> FetchResult.ERROR_AUTHENTICATION
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
            val filterState: FilterState,
        )
        private var instances: MutableMap<ArrivalsKey, Repository> = mutableMapOf()

        fun getInstance(
            apiProvider: Provider,
            apiValue: String,
            filterState: FilterState,
        ): Repository {
            val key = ArrivalsKey(apiProvider, apiValue, filterState)
            if (!instances.contains(key)) {
                instances[key] = Repository(
                    DataSource(
                        apiProvider.arrivalsApiConstructor(apiValue, filterState),
                        Dispatchers.IO
                    ),
                )
            }

            return instances[key] as Repository
        }
    }
}