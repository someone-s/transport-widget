package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.ArrivalsState
import com.eden.livewidget.data.common.DataPoint
import com.eden.livewidget.data.common.DestinationFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.collections.map

class TransitousArrivalsRepository(
    private val api: TransitousArrivalsApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ArrivalsRepository {
    private val states: MutableMap<Any, MutableStateFlow<ArrivalsState>> = mutableMapOf()

    private fun getOrCreateMutableState(key: Any): MutableStateFlow<ArrivalsState> = states.getOrPut(
        key = key,
        defaultValue = { MutableStateFlow(ArrivalsState()) }
    )
    override fun getOrCreateFlow(key: Any): StateFlow<ArrivalsState> = getOrCreateMutableState(key)

    override fun deleteState(key: Any) { states.remove(key) }

    override suspend fun updateState(
        key: Any,
        dataPoints: List<DataPoint>,
        destinationFilters: List<DestinationFilter>
    ): ArrivalsRepository.FetchResult {

        assert(dataPoints.size == 1) { "Must have exactly one data point" }
        val transitousPoint = dataPoints[0] as TransitousDataPoint
        val transitousDestinationFilters = destinationFilters.map { it as TransitousDestinationFilter }

        val (result, latestData) = withContext(dispatcher) {
            try {
                val data = api.fetchLatestArrivals(
                    transitousPoint.id,
                    transitousDestinationFilters.map { it.toId },
                )
                Pair(ArrivalsRepository.FetchResult.SUCCESS, data)
            } catch (_: TransitousArrivalsApi.UnresolvedException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: TransitousArrivalsApi.UnreachableException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNREACHABLE, null)
            }
        }

        getOrCreateMutableState(key).getAndUpdate { previousData ->
            if (latestData != null)
                ArrivalsState(LocalDateTime.now(), ArrivalsState.Validity.VALID, latestData)
            else
                ArrivalsState(LocalDateTime.now(), ArrivalsState.Validity.INVALID, previousData.lastValidData)
        }

        return result

    }

}