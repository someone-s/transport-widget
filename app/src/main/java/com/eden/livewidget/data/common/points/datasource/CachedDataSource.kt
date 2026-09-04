package com.eden.livewidget.data.common.points.datasource

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.common.points.Option
import com.eden.livewidget.data.common.points.cache.CacheProvider
import com.eden.livewidget.data.common.points.api.BufferedApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CachedDataSource(
    private val pointsBufferedApi: BufferedApi,
    private val cacheProvider: CacheProvider,
    private val ioDispatcher: CoroutineDispatcher
): DataSource, DataSource.Refreshable, DataSource.Resettable {

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
            pointsBufferedApi.fetchPoints(
                context,
                statusUpdate,
            ).collect { option ->
                cacheProvider.getCache(context).insert(option)
            }
        }

    }

    override suspend fun fetchMatching(
        context: Context,
        input: String
    ): List<Option> =
        withContext(ioDispatcher) {
            cacheProvider
                .getCache(context)
                .getAllFuzzyMatches(input)
        }
}

