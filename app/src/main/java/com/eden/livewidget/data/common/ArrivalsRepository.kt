package com.eden.livewidget.data.common

import com.eden.livewidget.data.common.arrivals.ArrivalsState
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter
import kotlinx.coroutines.flow.Flow

interface ArrivalsRepository {
    val arrivalsData: Flow<ArrivalsState>

    suspend fun fetchLatestArrival(
        dataPoints: List<DataPoint>,
        destinationFilters: List<DestinationFilter> = emptyList(),
    ): FetchResult

    enum class FetchResult {
        SUCCESS,
        ERROR_UNRESOLVED,
        ERROR_UNREACHABLE,
        ERROR_AUTHENTICATION,
    }

}