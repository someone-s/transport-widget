package com.eden.livewidget.data.buffered

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.room.RoomDatabase.JournalMode
import androidx.sqlite.db.SupportSQLiteDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteFunction
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class BufferedPointsDatabaseProvider<T: RoomDatabase>(
    private val klass: Class<T>,
    private val databaseName: String,
) {
    /**
     * assets/database/$databaseName (no file extension) text file must exist
     */
    @Volatile
    private var instance: T? = null

    fun getDatabase(context: Context): T {
        return instance ?: synchronized(this) {
            Room.databaseBuilder(context, klass, databaseName)
                .setJournalMode(JournalMode.TRUNCATE)
                .openHelperFactory { configuration ->

                    checkNotNull(configuration.name)

                    val config = SQLiteDatabaseConfiguration(
                        context.getDatabasePath(configuration.name).path,
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
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        tryPopulateCache(context, db)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        tryPopulateCache(context, db)
                    }
                })
                .build()
                .also { instance = it }
        }
    }

    private fun tryPopulateCache(
        context: Context,
        database: SupportSQLiteDatabase,
    ) {
        // not packaging binary of the database and use createFromAsset to comply with f-droid no binary during build rules
        Log.i(javaClass.name, "Open Writable DB OK")

        context.assets.open("database/$databaseName")
            .use { inputStream ->

                Log.i(javaClass.name, "Open InputStream OK")
                BufferedReader(
                    InputStreamReader(inputStream, StandardCharsets.UTF_8)
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

    fun deleteDatabase(context: Context) = synchronized(this) {
        context.deleteDatabase(databaseName)
        instance = null
    }
}