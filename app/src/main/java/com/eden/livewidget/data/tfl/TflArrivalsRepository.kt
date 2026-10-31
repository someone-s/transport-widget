package com.eden.livewidget.data.tfl

import com.eden.livewidget.data.common.arrivals.ArrivalsState
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter
import com.eden.livewidget.data.common.DataPoint
import com.eden.livewidget.data.common.ArrivalsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val _arrivalsData = MutableStateFlow(ArrivalsState())
    override val arrivalsData: Flow<ArrivalsState> = _arrivalsData.asStateFlow()

    override suspend fun fetchLatestArrival(
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
            } catch (_: Api.UnresolvedException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: Api.UnreachableException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNREACHABLE, null)
            } catch (_: Api.AuthenticationException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_AUTHENTICATION, null)
            }
        }

        _arrivalsData.getAndUpdate { previousData ->
            if (latestData != null)
                ArrivalsState(LocalDateTime.now(), ArrivalsState.Validity.VALID, latestData)
            else
                ArrivalsState(LocalDateTime.now(), ArrivalsState.Validity.INVALID, previousData.lastValidData)
        }

        return result
    }
}