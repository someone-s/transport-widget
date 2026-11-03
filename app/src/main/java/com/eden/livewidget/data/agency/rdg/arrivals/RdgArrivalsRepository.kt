package com.eden.livewidget.data.agency.rdg.arrivals

import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsState
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.rdg.destination.RdgDestinationFilter
import com.eden.livewidget.data.agency.rdg.point.RdgDataPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.collections.map

class RdgArrivalsRepository(
    private val api: RdgArrivalsApi,
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
        val rdgPoint = dataPoints[0] as RdgDataPoint

        val crsCode = rdgPoint.crsCode
        val toCrsCodes = destinationFilters.map { it as RdgDestinationFilter }.map { it.toCrsCode }

        val (result, latestData) = withContext(dispatcher) {
            try {
                val data = api.fetchLatestArrivals(crsCode, toCrsCodes)
                Pair(ArrivalsRepository.FetchResult.SUCCESS, data)
            } catch (_: RdgArrivalsApi.UnresolvedException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNRESOLVED, null)
            } catch (_: RdgArrivalsApi.UnreachableException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_UNREACHABLE, null)
            } catch (_: RdgArrivalsApi.AuthenticationException) {
                Pair(ArrivalsRepository.FetchResult.ERROR_AUTHENTICATION, null)
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