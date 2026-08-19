package com.eden.livewidget.data.common.arrivals.filter

import kotlinx.serialization.Serializable
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter as DestinationFilter

@Serializable
data class State(
    val destinationFilters: List<DestinationFilter>,
)

fun emptyState() = State(
    destinationFilters = emptyList()
)