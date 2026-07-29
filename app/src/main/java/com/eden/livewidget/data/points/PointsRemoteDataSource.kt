package com.eden.livewidget.data.points

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PointsRemoteDataSource(
    context: Context,
    private val pointsApi: PointsRemoteApi,
    private val apiProvider: Provider,
    private val ioDispatcher: CoroutineDispatcher
): PointsDataSource {

    private var pointsDao = PointsCacheDatabase.getInstance(context, apiProvider).pointDao()

    override fun reset(context: Context) {
        Log.i(this.javaClass.name, "Database reset")
        PointsCacheDatabase.deleteDatabase(context, apiProvider)
        pointsDao = PointsCacheDatabase.getInstance(context, apiProvider).pointDao()
    }

    override suspend fun refresh(context: Context, statusUpdate: (status: String) -> Unit) {
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.

        pointsDao.deleteAll()

        pointsApi.fetchPoints(context, { pointsDao.insertAll(it) }, statusUpdate)

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
    suspend fun fetchPoints(context: Context, add: (PointEntity) -> Unit, statusUpdate: (status: String) -> Unit)
}