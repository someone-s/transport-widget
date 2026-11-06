package com.eden.livewidget.data.agency.rdg.destination

import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-rdg")
class RdgDestinationFilter(
    @SerialName("c")
    val toCrsCode: String,
): DestinationFilter