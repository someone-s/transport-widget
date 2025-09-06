package com.eden.livewidget.data.points

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.SkipQueryVerification
import com.eden.livewidget.data.Provider
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteFunction
import me.xdrop.fuzzywuzzy.FuzzySearch


@Entity()
data class PointEntity(
    @ColumnInfo(name = "name") val name: String,
    @PrimaryKey val apiValue: String, // api specific format id
)

@Dao
interface PointDao {
    @Query("SELECT * FROM pointentity")
    fun getAll(): List<PointEntity>

    @SkipQueryVerification
    @Query("SELECT *, fuzzy_ratio(lower(:search), name) AS SCORE FROM pointentity " +
            "WHERE SCORE > 50 " +
            "ORDER BY SCORE DESC")
    fun getAllFuzzyMatches(search: String): List<PointEntity>

    @Insert
    fun insertAll(vararg points: PointEntity)

    @Delete
    fun delete(point: PointEntity)

    @Query("DELETE FROM pointentity")
    fun deleteAll()
}

@Database(entities = [PointEntity::class], version = 1)
abstract class PointsCacheDatabase : RoomDatabase() {
    abstract fun pointDao(): PointDao

    companion object {

        private val instances = mutableMapOf<Provider, PointsCacheDatabase>()

        fun getInstance(context: Context, apiProvider: Provider): PointsCacheDatabase {
            if (!instances.contains(apiProvider))
                instances[apiProvider] = buildDatabase(context, apiProvider)

            return instances[apiProvider] as PointsCacheDatabase
        }

        fun getDatabaseName(apiProvider: Provider) = "${DATABASE_NAME_BASE}_${apiProvider}"

        private const val DATABASE_NAME_BASE = "PointsCacheDB"

        fun deleteDatabase(context: Context, apiProvider: Provider) {
            val databaseName = getDatabaseName(apiProvider)

            context.deleteDatabase(databaseName)

            instances.remove(apiProvider)
        }

        fun buildDatabase(context: Context, apiProvider: Provider): PointsCacheDatabase {
            val databaseName = getDatabaseName(apiProvider)

            val db = Room.databaseBuilder(
                context,
                PointsCacheDatabase::class.java,
                databaseName
            )
                .createFromAsset("database/$databaseName")
                .setJournalMode(JournalMode.TRUNCATE)
                .openHelperFactory { configuration ->
                    val config = SQLiteDatabaseConfiguration(
                        context.getDatabasePath(databaseName).path,
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
            return db
        }

    }
}
