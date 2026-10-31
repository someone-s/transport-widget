package com.eden.livewidget.data.rdg

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eden.livewidget.data.buffered.BufferedPointsDatabaseProvider

/**
 * This database is designed to support the following relationship:
 * 1 name -> 1 value set
 */
@Database(entities = [RdgPointsEntity::class], version = 9)
abstract class RdgPointsDatabase : RoomDatabase() {
    abstract fun pointDao(): RdgPointsDao

    companion object: BufferedPointsDatabaseProvider<RdgPointsDatabase>(
        RdgPointsDatabase::class.java,
        "RDG_CACHE_DATABASE"
    )
}

