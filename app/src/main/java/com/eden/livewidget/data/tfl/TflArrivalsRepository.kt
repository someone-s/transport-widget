package com.eden.livewidget.data.tfl

import com.eden.livewidget.data.common.DataPoint
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.ArrivalsState
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
    ): ArrivalsRepository.FetchResult {

        val tflDestinationFilters = destinationFilters.map { it as TflDestinationFilter }

        val naptanIds = dataPoints
            .map { it as TflDataPoint }
            .filter { point ->
                tflDestinationFilters
                    .map { filter -> filter.fromNaptanId }
                    .contains(point.naptanId)
            }
            .map { it.naptanId }

        val (result, latestData) = withContext(dispatcher) {
            try {
                val tflArrivalEntries = api.fetchLatestArrivals(naptanIds)

                val lineDirectionPairs = tflDestinationFilters.map { filter ->
                    Pair(filter.lineId, filter.direction)
                }
                val lineFinalPairs = tflDestinationFilters.flatMap { filter ->
                    filter.finalNaptanIds.map { finalNaptanId -> Pair(filter.lineId, finalNaptanId) }
                }
                val data = tflArrivalEntries
                    .filter { arrivalsEntry ->
                        if (arrivalsEntry.finalNaptanId != "")
                            lineFinalPairs.contains(Pair(arrivalsEntry.lineId, arrivalsEntry.finalNaptanId))
                        else
                            lineDirectionPairs.contains(Pair(arrivalsEntry.lineId, arrivalsEntry.direction))
                    }
                Pair(ArrivalsRepository.FetchResult.SUCCESS, data)
            } catch (_: TflArrivalsApi.UnresolvedException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: TflArrivalsApi.UnreachableException) {
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