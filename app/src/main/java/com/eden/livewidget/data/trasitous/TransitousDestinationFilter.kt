package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-t00")
class TransitousDestinationFilter(
    @SerialName("i")
    val toId: String,
): DestinationFilter {
}