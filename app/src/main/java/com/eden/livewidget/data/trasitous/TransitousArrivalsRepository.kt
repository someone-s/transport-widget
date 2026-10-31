package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.arrivals.ArrivalsState
import com.eden.livewidget.data.common.arrivals.api.Api
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.DataPoint
import com.eden.livewidget.data.transitous.points.TransitousDataPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.collections.map

class TransitousArrivalsRepository(
    private val api: TransitousArrivalsApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ArrivalsRepository {
    private val _arrivalsData = MutableStateFlow(ArrivalsState())
    override val arrivalsData: Flow<ArrivalsState> = _arrivalsData.asStateFlow()

    override suspend fun fetchLatestArrival(
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