package com.eden.livewidget.data.rdg.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.destination.Compiler
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter.Companion.cloneApplied
import com.eden.livewidget.data.rdg.points.RdgValue as PointsRdgValue

class RdgCompiler: Compiler {
    override suspend fun compileFilter(filter: Filter): Filter {

        assert(filter.toPoint.values.size == 1)
        val value = filter.toPoint.values[0] as PointsRdgValue

        return filter.cloneApplied(
            listOf(
                RdgValue(
                    toCrsCode = value.crsCode,
                )
            )
        )
    }
}