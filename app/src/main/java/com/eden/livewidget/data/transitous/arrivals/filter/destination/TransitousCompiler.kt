package com.eden.livewidget.data.transitous.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.destination.Compiler
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter.Companion.cloneApplied
import com.eden.livewidget.data.transitous.points.TransitousValue as PointsTransitousValue

class TransitousCompiler: Compiler {
    override suspend fun compileFilter(filter: Filter): Filter {

        assert(filter.toPoint.values.size == 1)
        val value = filter.toPoint.values[0] as PointsTransitousValue

        return filter.cloneApplied(
            listOf(
                TransitousValue(
                    toId = value.id,
                )
            )
        )
    }
}