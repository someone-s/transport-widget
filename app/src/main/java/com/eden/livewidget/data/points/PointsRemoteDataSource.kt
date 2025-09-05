package com.eden.livewidget.data.points

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PointsRemoteDataSource(
    private val pointsApi: PointsRemoteApi,
    private val ioDispatcher: CoroutineDispatcher
): PointsDataSource {

    override suspend fun fetchMatching(input: String): List<PointModel> =
    // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            pointsApi.fetchMatching(input)
        }
}

interface PointsRemoteApi {
    fun fetchMatching(input: String): List<PointModel>
}