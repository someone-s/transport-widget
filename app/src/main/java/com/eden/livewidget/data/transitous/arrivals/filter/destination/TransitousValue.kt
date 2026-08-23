package com.eden.livewidget.data.transitous.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.destination.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-t00")
class TransitousValue(
    @SerialName("i")
    val toId: String,
): Value {
}