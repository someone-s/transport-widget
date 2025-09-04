// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data

import com.eden.livewidget.api.LivePointTflApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LivePointRepository(
    private val livePointDataSource: LivePointDataSource,
) {
    val lastestArrivals: Flow<List<ArrivalModel>> =
        livePointDataSource.latestArrivals

//    private val livePointMutex = Mutex()
//
//    private val livePointModels: MutableMap<String, LivePointModel> = mutableMapOf()
//
//    suspend fun getLivePointModel(stopPointId: String, refresh: Boolean = false): LivePointModel {
//        return if (refresh || !livePointModels.contains(stopPointId)) {
//            externalScope.async {
//                livePointDataSource.fetchLatestArrival(stopPointId).also { networkResult ->
//                    livePointMutex.withLock {
//                        livePointModels[stopPointId] = networkResult
//                    }
//                }
//            }.await()
//        } else {
//            return livePointMutex.withLock {
//                val model = livePointModels[stopPointId]
//                assert(model != null)
//                model as LivePointModel
//            }
//        }
//
//    }
//
//    suspend fun fetchLivePointData(stopPointId: String) {
//        livePointDataSource.fetchLatestArrival(stopPointId)
//    }

    companion object {
        private var instances: MutableMap<String, LivePointRepository> = mutableMapOf()

        fun getInstance(stopPointId: String): LivePointRepository {
            if (!instances.contains(stopPointId))
                instances[stopPointId] = LivePointRepository(
                    LivePointDataSource(
                        LivePointTflApi(stopPointId)
                    ),
                )

            return instances[stopPointId] as LivePointRepository
        }
    }
}