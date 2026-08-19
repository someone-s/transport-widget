package com.eden.livewidget.data.tfl.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.destination.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-tfl")
class TflValue(
    val lineId: String,
    val fromNaptanId: String,
    val direction: String,
): Value {
}