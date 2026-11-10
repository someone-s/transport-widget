package com.eden.livewidget.data.agency.common

import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsState
import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.common.keys.Key
import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.point.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

val mockProvider = object : Provider {
    override val keys: List<Key> = emptyList()
    override val pointsRepository: PointsRepository = object : PointsRepository {
        override val matchingOptions: Flow<List<PointsRepository.Option>> = flowOf(emptyList())
        override suspend fun fetchMatching(input: String) {}
    }
    override val arrivalsRepository: ArrivalsRepository = object : ArrivalsRepository {
        override fun getOrCreateFlow(key: Any): StateFlow<ArrivalsState> = MutableStateFlow(ArrivalsState())

        override fun deleteState(key: Any) {}

        override suspend fun updateState(
            key: Any,
            dataPoints: List<DataPoint>,
            destinationFilters: List<DestinationFilter>
        ): FetchResult = FetchResult.ERROR_NO_DATA
    }
    override val destinationCompiler: DestinationCompiler = object : DestinationCompiler {
        override suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter> = emptyList()
    }
}