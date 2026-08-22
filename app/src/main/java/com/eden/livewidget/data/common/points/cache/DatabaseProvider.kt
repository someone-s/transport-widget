package com.eden.livewidget.data.common.points.cache

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteFunction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class DatabaseProvider<T>(
    val info: DatabaseInfo,
    val klass: Class<T>,
): CacheProvider where T: RoomDatabase, T: Cache {

    private class State<T> {
        var currentDatabase: T? = null
        val mutex = Mutex()
    }

    companion object {
        private val states = mutableMapOf<Class<*>, State<*>>()

        @Suppress("UNCHECKED_CAST")
        private fun <T> getState(klass: Class<T>): State<T> = states.getOrPut(klass) { State<T>() } as State<T>
    }

    override suspend fun getCache(context: Context): Cache {

        val state = getState(klass)

        state.mutex.withLock {
            if (state.currentDatabase == null)
                state.currentDatabase = constructCache(context)

            return state.currentDatabase!!
        }
    }

    private fun constructCache(
        applicationContext: Context,
    ): T {

        val database = Room.databaseBuilder(
            applicationContext,
            klass,
            info.databaseName,
        )
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .openHelperFactory { configuration ->
                val config = SQLiteDatabaseConfiguration(
                    applicationContext.getDatabasePath(info.databaseName).path,
                    SQLiteDatabase.OPEN_CREATE or SQLiteDatabase.OPEN_READWRITE
                )

                // Custom Kotlin function
                config.functions.add(
                    SQLiteFunction("fuzzy_ratio", 2) { args, result ->
                        if (args != null && result != null) {
                            val str1 = args.getString(0)
                            val str2 = args.getString(1).lowercase()
                            val score = FuzzySearch.partialRatio(str1, str2)
                            result.set(score)
                        }
                    }
                )

                val options = RequerySQLiteOpenHelperFactory.ConfigurationOptions { config }
                RequerySQLiteOpenHelperFactory(listOf(options)).create(configuration)
            }
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    tryPopulateCache(db, applicationContext)
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    tryPopulateCache(db, applicationContext)
                }
            })
            .build()

        return database
    }

    private fun tryPopulateCache(
        database: SupportSQLiteDatabase,
        applicationContext: Context,
    ) {
        // exit if no populate file defined
        if (info.populateFileName == null) return

        // not packaging binary of the database and use createFromAsset to comply with f-droid no binary during build rules
        Log.i(javaClass.name, "Open Writable DB OK")

        applicationContext.assets.open("database/${info.populateFileName}")
            .use { inputStream ->

                Log.i(javaClass.name, "Open InputStream OK")
                BufferedReader(
                    InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                    )
                ).use { reader ->
                    Log.i(javaClass.name, "Open BufferReader OK")
                    var line = reader.readLine()
                    while (line != null) {
                        database.execSQL(line)
                        line = reader.readLine()
                    }
                }
            }

        Log.i(javaClass.name, "Load packaged data Complete")
    }

    override suspend fun removeCache(context: Context) {

        val state = getState(klass)

        state.mutex.withLock {
            context.deleteDatabase(info.databaseName)

            state.currentDatabase = null
        }
    }

}