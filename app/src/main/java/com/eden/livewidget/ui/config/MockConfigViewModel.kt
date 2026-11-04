package com.eden.livewidget.ui.config

import com.eden.livewidget.data.AppContainer
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.data.widget.repository.WidgetRepository
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val mockConfigViewModel = ConfigViewModel(
    appContainer = object : AppContainer {
        override val agencies: List<Agency> = emptyList()

        override fun agencyFromId(id: String): Agency? = null

        override val widgetRepository: WidgetRepository = object : WidgetRepository {
            override suspend fun getConfig(appWidgetId: Int): WidgetConfig? = null

            override fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfig?> = flowOf(null)

            override suspend fun setConfig(config: WidgetConfig) {}

            override suspend fun getState(appWidgetId: Int): WidgetState? = null

            override fun getStateFlow(appWidgetId: Int): Flow<WidgetState?> = flowOf(null)

            override suspend fun isAnyActive(): Boolean = false

            override suspend fun setIsActive(appWidgetId: Int, isActive: Boolean) {}

            override suspend fun setFetchState(appWidgetId: Int, fetchState: FetchState) {}
        }
    }
)