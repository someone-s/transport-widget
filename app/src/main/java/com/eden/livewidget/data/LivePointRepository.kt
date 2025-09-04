// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data

import com.eden.livewidget.api.LivePointTflApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LivePointRepository(
    private val livePointDataSource: LivePointDataSource,
) {
    private val latestArrivalsMutable = MutableStateFlow(emptyList<ArrivalModel>())
    val latestArrivals = latestArrivalsMutable.asStateFlow()

    suspend fun fetchLatestArrival() {
        latestArrivalsMutable.update { livePointDataSource.fetchLatestArrivals() }

    }

    companion object {
        private var instances: MutableMap<String, LivePointRepository> = mutableMapOf()

        fun getInstance(stopPointId: String): LivePointRepository {
            if (!instances.contains(stopPointId))
                instances[stopPointId] = LivePointRepository(
                    LivePointDataSource(
                        LivePointTflApi(stopPointId),
                        Dispatchers.IO
                    ),
                )

            return instances[stopPointId] as LivePointRepository
        }
    }
}