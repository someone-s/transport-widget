package com.eden.livewidget.data.rdg.arrivals

import com.eden.livewidget.data.rdg.points.RdgValue as PointsRdgValue

class AugmentedRdgValue(
    crsCode: String,
    val toCrsCodes: List<String>,
): PointsRdgValue(
    crsCode = crsCode
)