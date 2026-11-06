package com.eden.livewidget.data.agency.tfl.destination

import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-tfl")
class TflDestinationFilter(
    @SerialName("l")
    val lineId: String,
    @SerialName("f")
    val fromNaptanId: String,
    @SerialName("d")
    val direction: String,
    @SerialName("t")
    val finalNaptanIds: List<String>,
): DestinationFilter {
}