package com.eden.livewidget.data.widget.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ConfigEntity::class, StateEntity::class], version = 2)
abstract class WidgetDatabase : RoomDatabase() {
    protected abstract fun internalWidgetDao(): InternalWidgetDao

    fun widgetDao(): WidgetDao = internalWidgetDao()

    companion object {
        @Volatile
        private var Instance: WidgetDatabase? = null

        fun getDatabase(context: Context): WidgetDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    WidgetDatabase::class.java,
                    "WIDGET_DATABASE"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

