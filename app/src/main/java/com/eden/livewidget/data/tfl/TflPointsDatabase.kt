package com.eden.livewidget.data.tfl

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eden.livewidget.data.buffered.BufferedPointsDatabaseProvider

/**
 * This database is designed to support the following relationship:
 * 1 name -> many value
 */
@Database(entities = [TflPointsEntity::class], version = 9)
abstract class TflPointsDatabase : RoomDatabase() {
    abstract fun pointDao(): TflPointsDao

    companion object: BufferedPointsDatabaseProvider<TflPointsDatabase>(
        TflPointsDatabase::class.java,
        "TFL_CACHE_DATABASE"
    )
}

