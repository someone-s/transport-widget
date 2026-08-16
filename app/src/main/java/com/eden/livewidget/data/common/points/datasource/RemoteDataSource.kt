package com.eden.livewidget.data.common.points.datasource

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.cache.CacheProvider
import com.eden.livewidget.data.common.points.api.Api
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteDataSource(
    private val pointsApi: Api,
    private val cacheProvider: CacheProvider,
    private val ioDispatcher: CoroutineDispatcher
): DataSource {

    override suspend fun reset(context: Context) {
        Log.i(this.javaClass.name, "Database reset")
        cacheProvider.removeCache(context)
        cacheProvider.getCache(context)
    }

    override suspend fun refresh(context: Context, statusUpdate: (status: String) -> Unit) {
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.

        cacheProvider.getCache(context).deleteAll()

        withContext(ioDispatcher) {
            pointsApi.fetchPoints(
                context,
                statusUpdate,
            ).collect { point ->
                cacheProvider.getCache(context).insert(point)
            }
        }

    }

    override suspend fun fetchMatching(
        context: Context,
        input: String
    ): List<Model> =
        withContext(ioDispatcher) {
            cacheProvider
                .getCache(context)
                .getAllFuzzyMatches(input).map { entity ->
                    Model(
                        name = entity.name,
                        apiValue = entity.apiValue,
                    )
                }
        }
}

