package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.DestinationCompiler
import com.eden.livewidget.data.common.DestinationConfig
import com.eden.livewidget.data.common.DestinationFilter

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