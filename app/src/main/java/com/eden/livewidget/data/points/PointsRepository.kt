package com.eden.livewidget.data.points

import android.content.Context
import com.eden.livewidget.data.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PointsRepository(
    private val pointsDataSource: PointsDataSource,
) {
    private val matchingPointsMutable = MutableStateFlow(emptyList<PointModel>())
    val matchingPoints = matchingPointsMutable.asStateFlow()

    private val mutex = Mutex()
    private var queued: String? = null
    private var fetching = false
    suspend fun fetchMatching(input: String) {
        mutex.withLock {
            if (fetching) {
                queued = input
                return
            }
            fetching = true
        }
        matchingPointsMutable.update { pointsDataSource.fetchMatching(input) }

        var queuedCopy: String?
        mutex.withLock {
            fetching = false
            queuedCopy = queued
            queued = null
        }

        // Probably race condition, but not significant
        if (queuedCopy != null)
            fetchMatching(queuedCopy)
    }

    suspend fun refresh(context: Context, statusUpdate: (status: String) -> Unit) {
        pointsDataSource.refresh(context, statusUpdate)
    }


    fun reset(context: Context) {
        pointsDataSource.reset(context)
    }

    companion object {
        private var instances: MutableMap<Provider, PointsRepository> = mutableMapOf()

        fun getInstance(context: Context, apiProvider: Provider): PointsRepository {
            if (!instances.contains(apiProvider))
                instances[apiProvider] = PointsRepository(
                    apiProvider.pointsDataSourceConstructor(context)
                )


            return instances[apiProvider] as PointsRepository
        }
    }
}

interface PointsDataSource {
    suspend fun fetchMatching(input: String): List<PointModel>
    suspend fun refresh(context: Context, statusUpdate: (String) -> Unit)
    fun reset(context: Context)
}