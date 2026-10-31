package com.eden.livewidget.data.trasitous

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

class TransitousPointsRepository(
    private val api: TransitousPointsApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): PointsRepository {
    private val _matchingPoints = MutableStateFlow<List<PointsRepository.Option>>(emptyList())

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
                api.getAllFuzzyMatches(input).map {
                    PointsRepository.Option(
                        logicalPoint = it.logicalPoint,
                        annotation = it.annotation,
                    )
                }
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
}