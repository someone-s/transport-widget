package com.eden.livewidget.data.points

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.points.cache.PointsCacheProvider
import com.eden.livewidget.data.points.remoteapi.PointsRemoteApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PointsRemoteDataSource(
    private val pointsApi: PointsRemoteApi,
    private val pointsCacheProvider: PointsCacheProvider,
    private val ioDispatcher: CoroutineDispatcher
): PointsDataSource {

    override suspend fun reset(context: Context) {
        Log.i(this.javaClass.name, "Database reset")
        pointsCacheProvider.removeCache(context)
        pointsCacheProvider.getCache(context)
    }

    override suspend fun refresh(context: Context, statusUpdate: (status: String) -> Unit) {
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.

        pointsCacheProvider.getCache(context).deleteAll()

        withContext(ioDispatcher) {
            pointsApi.fetchPoints(
                context,
                statusUpdate,
            ).collect { point ->
                pointsCacheProvider.getCache(context).insert(point)
            }
        }

    }

    override suspend fun fetchMatching(
        context: Context,
        input: String
    ): List<PointModel> =
        withContext(ioDispatcher) {
            pointsCacheProvider
                .getCache(context)
                .getAllFuzzyMatches(input).map { entity ->
                    PointModel(
                        name = entity.name,
                        apiValue = entity.apiValue,
                    )
                }
        }
}

