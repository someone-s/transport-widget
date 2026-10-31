package com.eden.livewidget.logic

import kotlinx.serialization.Serializable

@Serializable
data class FiltersState(
    val destinations: List<DestinationState>,
) {
}

fun emptyFilterState() = FiltersState(
    destinations = emptyList()
)