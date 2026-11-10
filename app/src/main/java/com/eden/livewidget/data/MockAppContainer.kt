package com.eden.livewidget.data

import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.data.location.repository.LocationRepository
import com.eden.livewidget.data.location.structure.Location
import com.eden.livewidget.data.version.repository.VersionRepository
import com.eden.livewidget.data.widget.repository.WidgetRepository
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object MockAppContainer : AppContainer {
    override val agencies: List<Agency> = emptyList()

    override fun agencyFromId(id: String): Agency? = null

    override val widgetRepository: WidgetRepository = object : WidgetRepository {
        override suspend fun getConfig(appWidgetId: Int): WidgetConfig? = null

        override fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfig?> = flowOf(null)

        override suspend fun setConfig(config: WidgetConfig) {}

        override suspend fun deleteConfig(appWidgetId: Int) {}

        override suspend fun getState(appWidgetId: Int): WidgetState? = null

        override fun getStateFlow(appWidgetId: Int): Flow<WidgetState?> = flowOf(null)

        override suspend fun isAnyActive(): Boolean = false

        override suspend fun setIsActive(appWidgetId: Int, isActive: Boolean) {}

        override suspend fun setFetchState(appWidgetId: Int, fetchState: FetchState) {}
    }

    override val locationRepository: LocationRepository = object : LocationRepository {
        override suspend fun getLocation(accuracy: LocationRepository.Accuracy) = Location(0.0, 0.0)
        override fun getLastKnownLocation(): Location = Location(0.0, 0.0)
    }

    override val versionRepository: VersionRepository = object : VersionRepository {
        override val appName: String = ""
        override val applicationId: String = ""
        override val versionName: String = ""
        override val versionCode: Int = 0
        override val isVersionCodeGreaterThanStored: Flow<Boolean?> = flowOf(false)

        override suspend fun storeBuildVersionCode() {}
    }
}