package com.eden.livewidget.data.common

import kotlinx.coroutines.flow.StateFlow

interface ArrivalsRepository {
    fun getOrCreateFlow(key: Any): StateFlow<ArrivalsState>

    fun deleteState(key: Any)

    suspend fun updateState(
        key: Any,
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