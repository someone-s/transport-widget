package com.eden.livewidget.data.points

import com.eden.livewidget.data.utils.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PointsRepository(
    private val pointsDataSource: PointsDataSource,
) {
    private val matchingPointsMutable = MutableStateFlow(emptyList<PointModel>())
    val matchingPoints = matchingPointsMutable.asStateFlow()

    suspend fun fetchMatching(input: String) {
        matchingPointsMutable.update { pointsDataSource.fetchMatching(input) }

    }

    companion object {
        private var instances: MutableMap<Provider, PointsRepository> = mutableMapOf()

        fun getInstance(apiProvider: Provider): PointsRepository {
            if (!instances.contains(apiProvider))
                instances[apiProvider] = PointsRepository(
                    apiProvider.pointsDataSourceConstructor()
                )

            return instances[apiProvider] as PointsRepository
        }
    }
}

interface PointsDataSource {
    suspend fun fetchMatching(input: String): List<PointModel>
}