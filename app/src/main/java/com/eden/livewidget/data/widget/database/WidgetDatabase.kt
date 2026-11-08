package com.eden.livewidget.data.widget.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [ConfigEntity::class, StateEntity::class],
    version = 5,
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
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
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

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            UPDATE state
            SET fetch_state = 'ERROR_NETWORK_AUTHENTICATION'
            WHERE fetch_state = 'ERROR_AUTHENTICATION'
        """.trimIndent())
        connection.execSQL("""
            UPDATE state
            SET fetch_state = 'ERROR_NETWORK_UNREACHABLE'
            WHERE fetch_state = 'ERROR_UNREACHABLE'
        """.trimIndent())
        connection.execSQL("""
            UPDATE state
            SET fetch_state = 'ERROR_NETWORK_UNRESOLVED'
            WHERE fetch_state = 'ERROR_UNRESOLVED'
        """.trimIndent())
        connection.execSQL("""
            UPDATE state
            SET fetch_state = 'ERROR_NETWORK_METERED'
            WHERE fetch_state = 'ERROR_METERED'
        """.trimIndent())
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            UPDATE config
            SET point = replace(point, '"name":"', '"nameString":"')
        """.trimIndent())
    }
}