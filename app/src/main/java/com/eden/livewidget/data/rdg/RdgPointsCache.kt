package com.eden.livewidget.data.rdg

import android.content.Context
import com.eden.livewidget.data.buffered.BufferedPointOption
import com.eden.livewidget.data.buffered.BufferedPointsCache
import com.eden.livewidget.data.common.LogicalPoint
import com.eden.livewidget.data.formatter

class RdgPointsCache(
    private val context: Context,
    ): BufferedPointsCache, BufferedPointsCache.Resettable {

    private val pointsDao: RdgPointsDao get() = RdgPointsDatabase.getDatabase(context).pointDao()


    override suspend fun getAllFuzzyMatches(search: String): List<BufferedPointOption> =
        pointsDao.getAllFuzzyMatches(search)
            .map { native ->
                BufferedPointOption(
                    logicalPoint = LogicalPoint(name = native.name, dataPoints = formatter.decodeFromString(native.values))
                )
            }

    override suspend fun insert(option: BufferedPointOption) =
        pointsDao.insert(
            RdgPointsEntity(
                name = option.logicalPoint.name,
                values = formatter.encodeToString(option.logicalPoint.dataPoints)
            )
        )

    override suspend fun deleteAll() =
        pointsDao.deleteAll()

    override suspend fun reset() {
        RdgPointsDatabase.deleteDatabase(context)
        RdgPointsDatabase.getDatabase(context)
    }
}