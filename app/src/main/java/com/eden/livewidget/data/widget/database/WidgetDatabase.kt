package com.eden.livewidget.data.widget.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [ConfigEntity::class, StateEntity::class],
    version = 3,
    autoMigrations = []
)
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
                    .addMigrations(
                        MIGRATION_2_3
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            UPDATE config
            SET point = '{"type": "l-fixed",' || substr(point, 2)
        """.trimIndent())
    }
}
