package com.eden.livewidget.data.tfl.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.PreFetchExecutor
import com.eden.livewidget.data.common.points.Value as PointsValue
import com.eden.livewidget.data.tfl.points.TflValue as PointsTflValue

class TflPreFetchExecutor(
    val values: List<TflValue>
): PreFetchExecutor {
    override fun execute(pointsValues: List<PointsValue>): List<PointsValue> =
        if (values.isEmpty())
            pointsValues
        else
            pointsValues
                .map { it as PointsTflValue }
                .filter { pointsValue ->
                    values
                        .map { value -> value.fromNaptanId }
                        .contains(pointsValue.naptanId)
                }
}