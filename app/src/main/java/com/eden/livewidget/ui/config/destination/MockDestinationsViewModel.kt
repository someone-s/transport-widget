package com.eden.livewidget.ui.config.destination

import com.eden.livewidget.data.common.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val mockDestinationsViewModel = DestinationsViewModel(
    pointsRepository = object : PointsRepository {
        override val matchingPoints: Flow<List<PointsRepository.Option>> = flowOf()

        override suspend fun fetchMatching(input: String) { }
    }
)