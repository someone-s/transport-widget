package com.eden.livewidget.data.common.points.cache

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

@Entity()
data class SimpleEntity(

    @ColumnInfo(name = "name")
    val name: String,

    @PrimaryKey
    @ColumnInfo(name = "apiValue")
    val apiValue: String,
)

@Dao
interface PointsCacheSimpleDao {

    @SkipQueryVerification
    @Query(
        "SELECT *, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM simpleentity " +
                "WHERE SCORE  > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<SimpleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: SimpleEntity)

    @Query("DELETE FROM simpleentity")
    fun deleteAll()
}

@Database(entities = [SimpleEntity::class], version = 1)
abstract class SimpleDatabase : RoomDatabase(), Cache {
    abstract fun pointDao(): PointsCacheSimpleDao

    override fun getAllFuzzyMatches(search: String): List<Model> =
        pointDao()
            .getAllFuzzyMatches(search)
            .map { native -> Model(name = native.name, apiValue = native.apiValue) }

    override fun insert(point: Model) =
        pointDao()
            .insert(SimpleEntity(name = point.name, apiValue = point.apiValue))

    override fun deleteAll() =
        pointDao()
            .deleteAll()

}

