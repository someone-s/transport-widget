package com.eden.livewidget.ui.config.point

import com.eden.livewidget.data.agency.common.point.PointsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val mockPointsViewModel = PointsViewModel(
    pointsRepository = object : PointsRepository {
        override val matchingPoints: Flow<List<PointsRepository.Option>> = flowOf()

        override suspend fun fetchMatching(input: String) { }
    }
)