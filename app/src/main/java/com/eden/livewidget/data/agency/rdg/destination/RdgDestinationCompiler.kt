package com.eden.livewidget.data.agency.rdg.destination

import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.rdg.point.RdgDataPoint

class RdgDestinationCompiler: DestinationCompiler {
    override suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter> {

        assert(filter.toPoint.dataPoints.size == 1)
        val value = filter.toPoint.dataPoints[0] as RdgDataPoint

        return listOf(RdgDestinationFilter(
            toCrsCode = value.crsCode,
        ))
    }
}