package com.eden.livewidget.data.widget.repository

import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    suspend fun getConfig(appWidgetId: Int): WidgetConfig?
    fun getConfigFlow(appWidgetId: Int): Flow<WidgetConfig?>
    suspend fun setConfig(config: WidgetConfig)

    suspend fun getState(appWidgetId: Int): WidgetState?
    fun getStateFlow(appWidgetId: Int): Flow<WidgetState?>
    suspend fun isAnyActive(): Boolean
    suspend fun setIsActive(appWidgetId: Int, isActive: Boolean)
    suspend fun setFetchState(appWidgetId: Int, fetchState: FetchState)
}