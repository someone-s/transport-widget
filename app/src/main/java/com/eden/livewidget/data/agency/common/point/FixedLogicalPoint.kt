package com.eden.livewidget.data.agency.common.point

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("l-fixed")
@Stable
data class FixedLogicalPoint(
    override val name: String,
    val dataPoints: List<DataPoint>, // api specific value
): LogicalPoint() {
    constructor(
        name: String,
        dataPoint: DataPoint,
    ): this(
        name = name,
        dataPoints = listOf(dataPoint)
    )
}