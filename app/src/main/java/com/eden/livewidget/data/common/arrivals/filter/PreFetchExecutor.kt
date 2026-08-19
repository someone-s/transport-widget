package com.eden.livewidget.data.common.arrivals.filter

import com.eden.livewidget.data.common.points.Value as PointsValue

interface PreFetchExecutor {
    fun execute(pointsValues: List<PointsValue>): List<PointsValue>
}