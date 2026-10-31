package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationCompiler
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationConfig
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationConfig.Companion.cloneApplied
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter
import com.eden.livewidget.data.transitous.points.TransitousDataPoint as PointsTransitousValue

class TransitousCompiler: DestinationCompiler {
    override suspend fun compileFilter(filter: DestinationConfig): DestinationFilter {

        assert(filter.toPoint.dataPoints.size == 1)
        val value = filter.toPoint.dataPoints[0] as PointsTransitousValue

        return filter.cloneApplied(
            listOf(
                TransitousDestinationFilter(
                    toId = value.id,
                )
            )
        )
    }
}