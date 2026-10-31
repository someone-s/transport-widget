package com.eden.livewidget.data.tfl

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.SkipQueryVerification

@Dao
interface TflPointsDao {

    @SkipQueryVerification
    @Query(
        "SELECT merged.name AS name, merged.concatValue AS value, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM " +
                "   ( " +
                "       SELECT name, '[' || group_concat(value, ',') || ']' AS concatValue " +
                "       FROM e " +
                "       GROUP BY name " +
                "   ) " +
                "   AS merged " +
                "WHERE SCORE > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<TflPointsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: TflPointsEntity)

    @Query("DELETE FROM e")
    fun deleteAll()
}