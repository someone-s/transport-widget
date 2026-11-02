package com.eden.livewidget.data.rdg

import com.eden.livewidget.data.common.DestinationFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-rdg")
class RdgDestinationFilter(
    @SerialName("c")
    val toCrsCode: String,
): DestinationFilter {
}