package com.eden.livewidget.data.transitous.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.PreFetchExecutor
import com.eden.livewidget.data.transitous.arrivals.AugmentedTransitousValue
import com.eden.livewidget.data.common.points.Value as PointsValue
import com.eden.livewidget.data.transitous.points.TransitousValue as PointsTransitousValue
import com.eden.livewidget.data.transitous.arrivals.filter.destination.TransitousValue as DestinationTransitousValue

class TransitousPreFetchExecutor(
    val values: List<DestinationTransitousValue>,
): PreFetchExecutor {
    override fun execute(pointsValues: List<PointsValue>): List<PointsValue> {
        assert(pointsValues.size == 1)
        val pointsValue = pointsValues[0] as PointsTransitousValue

        return listOf(
            AugmentedTransitousValue(
                id = pointsValue.id,
                toIds = values.map { it.toId },
            )
        )
    }

}