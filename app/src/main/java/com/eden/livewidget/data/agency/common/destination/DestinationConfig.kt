package com.eden.livewidget.data.agency.common.destination

import com.eden.livewidget.data.agency.common.point.LogicalPoint
import kotlinx.serialization.Serializable

@Serializable
data class DestinationConfig(
    val fromPoint: LogicalPoint,
    val toPoint: LogicalPoint,
)
