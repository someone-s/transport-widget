package com.eden.livewidget.data.rdg

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.SkipQueryVerification

@Dao
interface RdgPointsDao {

    @SkipQueryVerification
    @Query(
        "SELECT *, fuzzy_ratio(lower(:search), name) AS SCORE " +
                "FROM e " +
                "WHERE SCORE  > 50 " +
                "ORDER BY SCORE DESC "
    )
    fun getAllFuzzyMatches(search: String): List<RdgPointsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(point: RdgPointsEntity)

    @Query("DELETE FROM e")
    fun deleteAll()
}