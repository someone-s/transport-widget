package com.eden.livewidget.data.rdg.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.PreFetchExecutor
import com.eden.livewidget.data.rdg.arrivals.AugmentedRdgValue
import com.eden.livewidget.data.common.points.Value as PointsValue
import com.eden.livewidget.data.rdg.points.RdgValue as PointsRdgValue
import com.eden.livewidget.data.rdg.arrivals.filter.destination.RdgValue as DestinationRdgValue

class RdgPreFetchExecutor(
    val values: List<DestinationRdgValue>,
): PreFetchExecutor {
    override fun execute(pointsValues: List<PointsValue>): List<PointsValue> {
        assert(pointsValues.size == 1)
        val pointsValue = pointsValues[0] as PointsRdgValue

        return listOf(
             AugmentedRdgValue(
                crsCode = pointsValue.crsCode,
                toCrsCodes = values.map { it.toCrsCode },
            )
        )
    }

}