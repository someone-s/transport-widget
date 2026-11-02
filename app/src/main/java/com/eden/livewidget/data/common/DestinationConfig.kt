package com.eden.livewidget.data.common

import kotlinx.serialization.Serializable

@Serializable
data class DestinationConfig(
    val fromPoint: LogicalPoint,
    val toPoint: LogicalPoint,
)
