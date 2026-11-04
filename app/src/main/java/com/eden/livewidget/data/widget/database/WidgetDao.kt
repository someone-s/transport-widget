package com.eden.livewidget.data.widget.database

import kotlinx.coroutines.flow.Flow

interface WidgetDao {

    suspend fun getConfig(appWidgetId: Int): ConfigEntity?

    fun getConfigFlow(appWidgetId: Int): Flow<ConfigEntity?>

    suspend fun setConfig(config: ConfigEntity)

    suspend fun deleteConfig(appWidgetId: Int)

    suspend fun getState(appWidgetId: Int): StateEntity?

    fun getStateFlow(appWidgetId: Int): Flow<StateEntity?>

    suspend fun setIsActive(appWidgetId: Int, isActive: Boolean)

    suspend fun setFetchState(appWidgetId: Int, fetchState: String)

    suspend fun getActiveCount(): Int
}