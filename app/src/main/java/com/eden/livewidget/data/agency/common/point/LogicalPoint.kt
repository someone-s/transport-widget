package com.eden.livewidget.data.agency.common.point

import kotlinx.serialization.Serializable

@Serializable
data class LogicalPoint(
    val name: String,
    val dataPoints: List<DataPoint>, // api specific value
) {
    constructor(
        name: String,
        dataPoint: DataPoint,
    ): this(
        name = name,
        dataPoints = listOf(dataPoint)
    )
}