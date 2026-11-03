package com.eden.livewidget.data.agency.trasitous.destination

import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.trasitous.point.TransitousDataPoint

class TransitousDestinationCompiler: DestinationCompiler {
    override suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter> {

        assert(filter.toPoint.dataPoints.size == 1)
        val value = filter.toPoint.dataPoints[0] as TransitousDataPoint

        return listOf(
            TransitousDestinationFilter(
                toId = value.id,
            )
        )
    }
}