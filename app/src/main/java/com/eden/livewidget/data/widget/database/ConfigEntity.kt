package com.eden.livewidget.data.widget.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "config",
)
data class ConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "app_widget_id")
    val appWidgetId: Int,
    @ColumnInfo(name = "agency_id")
    val agencyId: String?,
    @ColumnInfo(name = "point")
    val pointString: String?,
    @ColumnInfo(name = "filters")
    val filtersString: String?,
    val fetchDuration: Long,
    val fetchInterval: Long,
)