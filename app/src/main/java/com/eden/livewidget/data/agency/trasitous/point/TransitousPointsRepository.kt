package com.eden.livewidget.data.agency.trasitous.point

import com.eden.livewidget.R
import com.eden.livewidget.data.agency.common.FetchResult
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.common.point.FloatLogicalPoint
import com.eden.livewidget.data.agency.common.point.PointsRepository
import com.eden.livewidget.data.agency.common.text.LiteralText
import com.eden.livewidget.data.agency.common.text.ResourceText
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
): PointsRepository, PointsRepository.LocationAware {
    private val _matchingPoints = MutableStateFlow<List<PointsRepository.Option>>(emptyList())

    override val matchingOptions: Flow<List<PointsRepository.Option>> = _matchingPoints.asStateFlow()

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
                listOf(
                    PointsRepository.Option(
                        logicalPoint = FloatLogicalPoint(
                            name = ResourceText(R.string.agency_t00_point_livelocation_name)
                        ),
                        annotation = ResourceText(R.string.agency_t00_point_livelocation_annotation)
                    )
                ) +
                api.getAllFuzzyMatches(input).map {
                    PointsRepository.Option(
                        logicalPoint = it.logicalPoint,
                        annotation = it.annotation?.let { a -> LiteralText(a) },
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

    override suspend fun getClosest(
        latitude: Double,
        longitude: Double
    ): Pair<FetchResult, FixedLogicalPoint?> = withContext(dispatcher) {
        try {
            val data = api.getClosest(latitude, longitude)
            Pair(FetchResult.SUCCESS, data)
        } catch (_: TransitousPointsApi.UnreachableException) {
            Pair(FetchResult.ERROR_UNRESOLVED, null)
        } catch (_: TransitousPointsApi.UnreachableException) {
            Pair(FetchResult.ERROR_UNREACHABLE, null)
        } catch (_: TransitousPointsApi.SchemaException) {
            Pair(FetchResult.ERROR_SCHEMA, null)
        } catch (_: TransitousPointsApi.NoResultException) {
            Pair(FetchResult.ERROR_NO_DATA, null)
        }
    }
}