package com.eden.livewidget.data.common.points.datasource

import android.content.Context
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.points.PointModel

interface DataSource {
    suspend fun fetchMatching(context: Context, input: String): List<PointModel>
    suspend fun refresh(context: Context, statusUpdate: (String) -> Unit)
    suspend fun reset(context: Context)

    companion object {
        private var instances: MutableMap<Provider, DataSource> = mutableMapOf()

        fun getInstance(context: Context, apiProvider: Provider): DataSource {
            if (!instances.contains(apiProvider))
                instances[apiProvider] = apiProvider.dataSourceConstructor(context)


            return instances[apiProvider]!!
        }
    }
}