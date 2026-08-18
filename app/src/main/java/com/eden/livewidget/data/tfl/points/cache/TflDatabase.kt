package com.eden.livewidget.data.tfl.points.cache

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.SkipQueryVerification
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.cache.Cache
import com.eden.livewidget.data.pointsFormat

@Entity()
data class TflEntity(

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

@Dao
interface TflDao {

    @SkipQueryVerification
    @Query(
        "SELECT merged.name AS name, merged.concatValue AS value, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM " +
                "   ( " +
                "       SELECT name, '[' || group_concat(value, ',') || ']' AS concatValue " +
                "       FROM tflentity " +
                "       GROUP BY name " +
                "   ) " +
                "   AS merged " +
                "WHERE SCORE > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<TflEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: TflEntity)

    @Query("DELETE FROM tflentity")
    fun deleteAll()
}

/**
 * This database is designed to support the following relationship:
 * 1 name -> many value
 */
@Database(entities = [TflEntity::class], version = 5)
abstract class TflDatabase : RoomDatabase(), Cache {
    abstract fun pointDao(): TflDao

    override fun getAllFuzzyMatches(search: String): List<Model> =
        pointDao()
            .getAllFuzzyMatches(search)
            .map { native -> Model(name = native.name, values = pointsFormat.decodeFromString(native.value)) }

    override fun insert(point: Model) =
        point.values.forEach { value ->
            pointDao()
                .insert(TflEntity(name = point.name, value = pointsFormat.encodeToString(value)))
        }

    override fun deleteAll() =
        pointDao()
            .deleteAll()

}

