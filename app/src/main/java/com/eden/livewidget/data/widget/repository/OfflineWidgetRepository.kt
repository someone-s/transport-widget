package com.eden.livewidget.data.widget.repository

import com.eden.livewidget.data.widget.database.WidgetDao
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineWidgetRepository(private val widgetDao: WidgetDao): WidgetRepository {
    override suspend fun getConfig(appWidgetId: Int): WidgetConfig? =
        widgetDao.getConfig(appWidgetId)?.toWidgetConfig()

    override fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfig?> =
        widgetDao.getConfigFlow(appWidgetId).map { it?.toWidgetConfig() }

    override suspend fun setConfig(config: WidgetConfig) =
        widgetDao.setConfig(config.toConfigEntity())

    override suspend fun getState(appWidgetId: Int): WidgetState? =
        widgetDao.getState(appWidgetId)?.toWidgetState()

    override fun getStateFlow(appWidgetId: Int): Flow<WidgetState?> =
        widgetDao.getStateFlow(appWidgetId).map { it?.toWidgetState() }

    override suspend fun setState(
        appWidgetId: Int,
        state: WidgetState
    ) = widgetDao.setState(state.toStateEntity(appWidgetId))
}