package com.eden.livewidget.data.agency.trasitous.arrivals

import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsState
import com.eden.livewidget.data.agency.common.FetchResult
import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.trasitous.point.TransitousDataPoint
import com.eden.livewidget.data.agency.trasitous.destination.TransitousDestinationFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    ): FetchResult {

        assert(dataPoints.size == 1) { "Must have exactly one data point" }
        val transitousPoint = dataPoints[0] as TransitousDataPoint
        val transitousDestinationFilters = destinationFilters.map { it as TransitousDestinationFilter }

        val (result, latestData) = withContext(dispatcher) {
            try {
                val data = api.fetchLatestArrivals(
                    transitousPoint.id,
                    transitousDestinationFilters.map { it.toId },
                )
                Pair(FetchResult.SUCCESS, data)
            } catch (_: TransitousArrivalsApi.UnresolvedException) {
                Pair(FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: TransitousArrivalsApi.UnreachableException) {
                Pair(FetchResult.ERROR_UNREACHABLE, null)
            }
        }

        getOrCreateMutableState(key).getAndUpdate { previousData ->
            ArrivalsState(
                lastUpdate = LocalDateTime.now(),
                lastResult = result,
                validity =
                    if (latestData != null) ArrivalsState.Validity.VALID
                    else ArrivalsState.Validity.INVALID,
                lastValidData = latestData ?: previousData.lastValidData,
            )
        }

        return result

    }

}