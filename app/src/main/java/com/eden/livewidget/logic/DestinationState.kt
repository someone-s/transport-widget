package com.eden.livewidget.logic

import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import kotlinx.serialization.Serializable

@Serializable
data class DestinationState(
    val config: DestinationConfig,
    val filters: List<DestinationFilter>? = null,
)