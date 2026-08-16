package com.eden.livewidget.data.points.datasource

import android.content.Context
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.points.PointModel

interface PointsDataSource {
    suspend fun fetchMatching(context: Context, input: String): List<PointModel>
    suspend fun refresh(context: Context, statusUpdate: (String) -> Unit)
    suspend fun reset(context: Context)

    companion object {
        private var instances: MutableMap<Provider, PointsDataSource> = mutableMapOf()

        fun getInstance(context: Context, apiProvider: Provider): PointsDataSource {
            if (!instances.contains(apiProvider))
                instances[apiProvider] = apiProvider.pointsDataSourceConstructor(context)


            return instances[apiProvider]!!
        }
    }
}