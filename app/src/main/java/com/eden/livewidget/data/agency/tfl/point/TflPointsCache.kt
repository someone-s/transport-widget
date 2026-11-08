package com.eden.livewidget.data.agency.tfl.point

import android.content.Context
import com.eden.livewidget.data.agency.buffered.point.BufferedPointOption
import com.eden.livewidget.data.agency.buffered.point.BufferedPointsCache
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.formatter

class TflPointsCache(
    private val context: Context,
    ): BufferedPointsCache, BufferedPointsCache.Resettable {

    private val pointsDao: TflPointsDao get() = TflPointsDatabase.getDatabase(context).pointDao()


    override suspend fun getAllFuzzyMatches(search: String): List<BufferedPointOption> =
        pointsDao.getAllFuzzyMatches(search)
            .map { native ->
                BufferedPointOption(
                    fixedLogicalPoint = FixedLogicalPoint(
                        nameString = native.name,
                        dataPoints = formatter.decodeFromString(native.value)
                    )
                )
            }

    override suspend fun insert(option: BufferedPointOption) =
        option.fixedLogicalPoint.dataPoints.forEach { value ->
            pointsDao.insert(
                TflPointsEntity(
                    name = option.fixedLogicalPoint.nameString,
                    value = formatter.encodeToString(value)
                )
            )
        }

    override suspend fun deleteAll() =
        pointsDao.deleteAll()

    override suspend fun reset() {
        TflPointsDatabase.deleteDatabase(context)
        TflPointsDatabase.getDatabase(context)
    }
}