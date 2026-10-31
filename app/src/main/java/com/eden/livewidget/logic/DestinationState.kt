package com.eden.livewidget.logic

import com.eden.livewidget.data.common.DestinationConfig
import com.eden.livewidget.data.common.DestinationFilter
import kotlinx.serialization.Serializable

@Serializable
data class DestinationState(
    val config: DestinationConfig,
    val filters: List<DestinationFilter>? = null,
)