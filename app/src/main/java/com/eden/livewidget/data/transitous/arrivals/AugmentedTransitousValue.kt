package com.eden.livewidget.data.transitous.arrivals

import com.eden.livewidget.data.transitous.points.TransitousValue as PointsTransitousValue

class AugmentedTransitousValue(
    id: String,
    val toIds: List<String>,
): PointsTransitousValue(
    id = id,
)