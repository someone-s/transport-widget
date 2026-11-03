package com.eden.livewidget.data.widget.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.SkipQueryVerification
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

interface WidgetDao {

    suspend fun getConfig(appWidgetId: Int): ConfigEntity?

    fun getConfigFlow(appWidgetId: Int): Flow<ConfigEntity?>

    suspend fun setConfig(config: ConfigEntity)

    suspend fun deleteConfig(appWidgetId: Int)

    suspend fun getState(appWidgetId: Int): StateEntity?

    fun getStateFlow(appWidgetId: Int): Flow<StateEntity?>

    suspend fun setState(state: StateEntity)
}