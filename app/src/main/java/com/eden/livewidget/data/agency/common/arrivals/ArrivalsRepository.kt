package com.eden.livewidget.data.agency.common.arrivals

import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import kotlinx.coroutines.flow.StateFlow

interface ArrivalsRepository {
    fun getOrCreateFlow(key: Any): StateFlow<ArrivalsState>

    fun deleteState(key: Any)

    suspend fun updateState(
        key: Any,
        dataPoints: List<DataPoint>,
        destinationFilters: List<DestinationFilter> = emptyList(),
    ): FetchResult

}