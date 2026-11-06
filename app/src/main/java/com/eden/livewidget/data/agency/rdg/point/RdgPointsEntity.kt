package com.eden.livewidget.data.agency.rdg.point

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "e",
)
data class RdgPointsEntity(

    @ColumnInfo(name = "name")
    val name: String,

    @PrimaryKey
    @ColumnInfo(name = "value")
    val values: String,
)