package com.eden.livewidget.data.rdg.points.cache

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
import com.eden.livewidget.data.format

@Entity(
    tableName = "e",
)
data class RdgEntity(

    @ColumnInfo(name = "name")
    val name: String,

    @PrimaryKey
    @ColumnInfo(name = "value")
    val values: String,
)

@Dao
interface RdgDao {

    @SkipQueryVerification
    @Query(
        "SELECT *, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM e " +
                "WHERE SCORE  > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<RdgEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: RdgEntity)

    @Query("DELETE FROM e")
    fun deleteAll()
}

/**
 * This database is designed to support the following relationship:
 * 1 name -> 1 value set
 */
@Database(entities = [RdgEntity::class], version = 9)
abstract class RdgDatabase : RoomDatabase(), Cache {
    abstract fun pointDao(): RdgDao

    override fun getAllFuzzyMatches(search: String): List<Model> =
        pointDao()
            .getAllFuzzyMatches(search)
            .map { native -> Model(name = native.name, values = format.decodeFromString(native.values)) }

    override fun insert(point: Model) =
        pointDao()
            .insert(RdgEntity(name = point.name, values = format.encodeToString(point.values)))

    override fun deleteAll() =
        pointDao()
            .deleteAll()

}

