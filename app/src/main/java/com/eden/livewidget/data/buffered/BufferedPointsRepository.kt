package com.eden.livewidget.data.buffered

import android.content.Context
import com.eden.livewidget.data.common.PointsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BufferedPointsRepository(
    private val api: BufferedPointsApi,
    private val cache: BufferedPointsCache,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): PointsRepository, PointsRepository.Refreshable, PointsRepository.Resettable {
    private val _matchingPoints = MutableStateFlow(emptyList<PointsRepository.Option>())
    override val matchingPoints: Flow<List<PointsRepository.Option>> = _matchingPoints.asStateFlow()

    private val mutex = Mutex()
    private var queued: String? = null
    private var fetching = false
    override suspend fun fetchMatching(input: String) {
        mutex.withLock {
            if (fetching) {
                queued = input
                return
            }
            fetching = true
        }

        _matchingPoints.update {
            withContext(dispatcher) {
                cache.getAllFuzzyMatches(input).map { PointsRepository.Option(
                    logicalPoint = it.logicalPoint,
                    annotation = it.annotation,
                ) }
            }
        }

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

    override suspend fun refresh(statusUpdate: ((status: String) -> Unit)?) {
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.

        cache.deleteAll()

        withContext(dispatcher) {
            api.fetchPoints(
                statusUpdate,
            ).collect { option ->
                cache.insert(option)
            }
        }
    }

    override val asResettable: PointsRepository.Resettable?
        get() = super.asResettable?.let {
            if (cache.asResettable != null) it
            else null
        }

    override suspend fun reset(context: Context) {
        val resettableCache = cache.asResettable
        checkNotNull(resettableCache)

        resettableCache.reset()
    }
}