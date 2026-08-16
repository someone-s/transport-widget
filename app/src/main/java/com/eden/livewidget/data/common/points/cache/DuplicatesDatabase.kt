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
data class DuplicatesEntity(

    @ColumnInfo(name = "name")
    val name: String,

    /**
     * When return result, can be concatenated with comma for duplicate names
     */
    @PrimaryKey
    @ColumnInfo(name = "apiValue")
    val apiValue: String,
)

@Dao
interface DuplicatesDao {

    @SkipQueryVerification
    @Query(
        "SELECT merged.name AS name, merged.concatApiValue AS apiValue, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM " +
                "   ( " +
                "       SELECT name, group_concat(apiValue, ',') AS concatApiValue " +
                "       FROM duplicatesentity " +
                "       GROUP BY name " +
                "   ) " +
                "   AS merged " +
                "WHERE SCORE > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<DuplicatesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: DuplicatesEntity)

    @Query("DELETE FROM duplicatesentity")
    fun deleteAll()
}

@Database(entities = [DuplicatesEntity::class], version = 1)
abstract class DuplicatesDatabase : RoomDatabase(), Cache {
    abstract fun pointDao(): DuplicatesDao

    override fun getAllFuzzyMatches(search: String): List<Model> =
        pointDao()
            .getAllFuzzyMatches(search)
            .map { native -> Model(name = native.name, apiValue = native.apiValue) }

    override fun insert(point: Model) =
        pointDao()
            .insert(DuplicatesEntity(name = point.name, apiValue = point.apiValue))

    override fun deleteAll() =
        pointDao()
            .deleteAll()

}

