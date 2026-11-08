package com.eden.livewidget.data.agency.tfl.arrivals

import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsState
import com.eden.livewidget.data.agency.common.FetchResult
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.tfl.point.TflDataPoint
import com.eden.livewidget.data.agency.tfl.destination.TflDestinationFilter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.collections.flatMap
import kotlin.collections.map

class TflArrivalsRepository(
    private val api: TflArrivalsApi,
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

        val tflDestinationFilters = destinationFilters.map { it as TflDestinationFilter }

        val naptanIds = dataPoints
            .map { it as TflDataPoint }
            .let {
                if (tflDestinationFilters.isNotEmpty())
                    it.filter { point ->
                        tflDestinationFilters
                            .map { filter -> filter.fromNaptanId }
                            .contains(point.naptanId)
                    }
                else it
            }
            .map { it.naptanId }

        val (result, latestData) = withContext(dispatcher) {
            try {
                val tflArrivalEntries = api.fetchLatestArrivals(naptanIds)

                val data = tflArrivalEntries.let {
                    if (tflDestinationFilters.isNotEmpty()) {
                        val lineDirectionPairs = tflDestinationFilters.map { filter ->
                            Pair(filter.lineId, filter.direction)
                        }
                        val lineFinalPairs = tflDestinationFilters.flatMap { filter ->
                            filter.finalNaptanIds.map { finalNaptanId -> Pair(filter.lineId, finalNaptanId) }
                        }
                        it.filter { arrivalsEntry ->
                            if (arrivalsEntry.finalNaptanId != "")
                                lineFinalPairs.contains(Pair(arrivalsEntry.lineId, arrivalsEntry.finalNaptanId))
                            else
                                lineDirectionPairs.contains(Pair(arrivalsEntry.lineId, arrivalsEntry.direction))
                        }
                    }
                    else it
                }
                Pair(FetchResult.SUCCESS, data)
            } catch (_: TflArrivalsApi.UnresolvedException) {
                Pair(FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: TflArrivalsApi.UnreachableException) {
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