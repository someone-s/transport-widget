package com.eden.livewidget.data.agency.rdg.point

import android.content.Context
import com.eden.livewidget.data.agency.buffered.point.BufferedPointEntry
import com.eden.livewidget.data.agency.buffered.point.BufferedPointsCache
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.formatter

class RdgPointsCache(
    private val context: Context,
    ): BufferedPointsCache, BufferedPointsCache.Resettable {

    private val pointsDao: RdgPointsDao get() = RdgPointsDatabase.getDatabase(context).pointDao()


    override suspend fun getAllFuzzyMatches(search: String): List<BufferedPointEntry> =
        pointsDao.getAllFuzzyMatches(search)
            .map { native ->
                BufferedPointEntry(
                    fixedLogicalPoint = FixedLogicalPoint(
                        nameString = native.name,
                        dataPoints = formatter.decodeFromString(native.values)
                    )
                )
            }

    override suspend fun insert(option: BufferedPointEntry) =
        pointsDao.insert(
            RdgPointsEntity(
                name = option.fixedLogicalPoint.nameString,
                values = formatter.encodeToString(option.fixedLogicalPoint.dataPoints)
            )
        )

    override suspend fun deleteAll() =
        pointsDao.deleteAll()

    override suspend fun reset() {
        RdgPointsDatabase.deleteDatabase(context)
        RdgPointsDatabase.getDatabase(context)
    }
}