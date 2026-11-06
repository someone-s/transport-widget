package com.eden.livewidget.data.agency.common.destination

import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import kotlinx.serialization.Serializable

@Serializable
data class DestinationConfig(
    val fromPoint: FixedLogicalPoint,
    val toPoint: FixedLogicalPoint,
)
