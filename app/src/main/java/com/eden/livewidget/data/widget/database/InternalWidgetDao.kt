package com.eden.livewidget.data.widget.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InternalWidgetDao: WidgetDao {

    @Query("""
        SELECT * FROM config
        WHERE app_widget_id = :appWidgetId
    """)
    override suspend fun getConfig(appWidgetId: Int): ConfigEntity?

    @Query("""
        SELECT * FROM config
        WHERE app_widget_id = :appWidgetId
    """)
    override fun getConfigFlow(appWidgetId: Int): Flow<ConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: ConfigEntity, state: StateEntity)

    override suspend fun setConfig(config: ConfigEntity) = setConfig(config, StateEntity(config.appWidgetId))


    @Transaction
    @Query("""
        DELETE FROM config
        WHERE app_widget_id = :appWidgetId
    """)
    override suspend fun deleteConfig(appWidgetId: Int)

    @Query("""
        SELECT * FROM state
        WHERE app_widget_id = :appWidgetId
    """)
    override suspend fun getState(appWidgetId: Int): StateEntity?

    @Query("""
        SELECT * FROM state
        WHERE app_widget_id = :appWidgetId
    """)
    override fun getStateFlow(appWidgetId: Int): Flow<StateEntity?>

    @Query("""
        SELECT COUNT(*) FROM state
        WHERE is_active = 1
    """)
    override suspend fun getActiveCount(): Int

    @Update
    suspend fun setState(state: StateEntity)

    @Transaction
    override suspend fun setIsActive(appWidgetId: Int, isActive: Boolean) {
        val state = getState(appWidgetId) ?: StateEntity(appWidgetId)
        setState(state.copy(isActive = isActive))
    }

    override suspend fun setFetchState(appWidgetId: Int, fetchState: String) {
        val state = getState(appWidgetId) ?: StateEntity(appWidgetId)
        setState(state.copy(fetchState = fetchState))
    }
}