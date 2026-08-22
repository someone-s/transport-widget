package com.eden.livewidget.data.rdg.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.destination.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-rdg")
class RdgValue(
    @SerialName("c")
    val toCrsCode: String,
): Value {
}