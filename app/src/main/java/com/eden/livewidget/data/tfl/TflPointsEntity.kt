package com.eden.livewidget.data.tfl

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "e",
)
data class TflPointsEntity(

    @ColumnInfo(name = "name")
    val name: String,

    /**
     * Input must a single JSON object
     * Output is a JSON list
     */
    @PrimaryKey
    @ColumnInfo(name = "value")
    val value: String,
)