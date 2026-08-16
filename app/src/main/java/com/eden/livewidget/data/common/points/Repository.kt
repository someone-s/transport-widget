package com.eden.livewidget.data.common.points

import android.content.Context
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.points.datasource.DataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Repository(
    private val dataSource: DataSource,
) {

    private val matchingPointsMutable = MutableStateFlow(emptyList<Model>())
    val matchingPoints = matchingPointsMutable.asStateFlow()

    private val mutex = Mutex()
    private var queued: String? = null
    private var fetching = false
    suspend fun fetchMatching(context: Context, input: String) {
        mutex.withLock {
            if (fetching) {
                queued = input
                return
            }
            fetching = true
        }

        matchingPointsMutable.update { dataSource.fetchMatching(context, input) }

        var queuedCopy: String?
        mutex.withLock {
            fetching = false
            queuedCopy = queued
            queued = null
        }

        // Probably race condition, but not significant
        if (queuedCopy != null)
            fetchMatching(context, queuedCopy)
    }

    companion object {
        fun create(context: Context, apiProvider: Provider) =
            Repository(DataSource.getInstance(context, apiProvider))
    }
}

