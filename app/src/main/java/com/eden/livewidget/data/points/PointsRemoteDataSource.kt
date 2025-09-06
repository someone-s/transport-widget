package com.eden.livewidget.data.points

import com.eden.livewidget.data.utils.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PointsRemoteDataSource(
    private val pointsApi: PointsRemoteApi,
    private val apiProvider: Provider,
    cacheDatabase: PointsCacheDatabase,
    private val ioDispatcher: CoroutineDispatcher
): PointsDataSource {

    private val pointsDao = cacheDatabase.pointDao()

    override suspend fun refresh(statusUpdate: (status: String) -> Unit) {
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        pointsDao.deleteAll()
        val pageBatch = 10
        var pageSet = 0
        while(true) {

            var hasEmpty = false

            coroutineScope {
                for (i in 0..<pageBatch) {
                    launch {
                        withContext(ioDispatcher) {
                            if (pointsApi.fetchPage(pageSet * pageBatch + i, { pointsDao.insertAll(it) }) <= 0)
                                hasEmpty = true
                        }
                    }
                }

                statusUpdate("Fetching page ${pageSet * pageBatch} to ${pageSet * pageBatch + pageBatch - 1}")
            }

            if (hasEmpty)
                break

            pageSet++
        }

    }

    override suspend fun fetchMatching(input: String): List<PointModel> =
        withContext(ioDispatcher) {
            pointsDao.getAllFuzzyMatches(input).map { entity ->
                PointModel(
                    name = entity.name,
                    apiProvider = apiProvider,
                    apiValue = entity.apiValue
                )
            }
        }
}

interface PointsRemoteApi {
    fun fetchPage(pageZeroIndexed: Int, add: (PointEntity) -> Unit): Int
}