package com.eden.livewidget.data.points.cache

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eden.livewidget.data.Provider
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

class SimpleDatabaseProvider(
    val apiProvider: Provider,
): PointsCacheProvider {

    companion object {
        private const val DATABASE_NAME_BASE = "PointsCacheSimpleDatabase"
        fun getDatabaseName(apiProvider: Provider) = "${DATABASE_NAME_BASE}_${apiProvider}"
        fun getDatabaseAssetName(apiProvider: Provider) = "${DATABASE_NAME_BASE}_${apiProvider}_TEXT"

        private var currentDatabase: SimpleDatabase? = null
        private val mutex = Mutex()
    }

    override suspend fun getCache(context: Context): PointsCache {
        mutex.withLock {
            if (currentDatabase == null)
                currentDatabase = constructCache(context)

            return currentDatabase!!
        }
    }

    private fun constructCache(
        applicationContext: Context
    ): SimpleDatabase {
        val databaseName = getDatabaseName(apiProvider)

        val underlyingDatabaseAlreadyExist = applicationContext.databaseList().contains(databaseName)

        val database = Room.databaseBuilder(
            applicationContext,
            SimpleDatabase::class.java,
            databaseName
        )
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .openHelperFactory { configuration ->
                val config = SQLiteDatabaseConfiguration(
                    applicationContext.getDatabasePath(databaseName).path,
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
            .build()

        if (!underlyingDatabaseAlreadyExist)
            populateCache(database, applicationContext)


        return database
    }

    private fun populateCache(
        database: SimpleDatabase,
        applicationContext: Context,
    ) {
        val messageClassName = SimpleDatabase::class.java.name

        // not packaging binary of the database and use createFromAsset to comply with f-droid no binary during build rules
        database.openHelper.writableDatabase
            .use { writableDB ->
                Log.i(messageClassName, "Open Writable DB OK")

                applicationContext.assets.open("database/${getDatabaseAssetName(apiProvider)}")
                    .use { inputStream ->

                        Log.i(messageClassName, "Open InputStream OK")
                        BufferedReader(
                            InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                            )
                        ).use { reader ->
                            Log.i(messageClassName, "Open BufferReader OK")
                            var line = reader.readLine()
                            while (line != null) {
                                writableDB.execSQL(line)
                                line = reader.readLine()
                            }
                        }
                    }
            }

        Log.i(messageClassName, "Load packaged data Complete")
    }

    override suspend fun removeCache(context: Context) {
        mutex.withLock {
            val databaseName = getDatabaseName(apiProvider)

            context.deleteDatabase(databaseName)

            currentDatabase = null
        }
    }

}