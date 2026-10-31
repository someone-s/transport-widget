package com.eden.livewidget.data.rdg

import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationCompiler
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationConfig
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter

class RdgDestinationCompiler: DestinationCompiler {
    override suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter> {

        assert(filter.toPoint.dataPoints.size == 1)
        val value = filter.toPoint.dataPoints[0] as RdgDataPoint

        return listOf(RdgDestinationFilter(
            toCrsCode = value.crsCode,
        ))
    }
}