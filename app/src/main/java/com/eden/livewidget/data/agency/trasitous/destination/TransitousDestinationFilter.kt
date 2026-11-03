package com.eden.livewidget.data.agency.trasitous.destination

import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("f-d-t00")
class TransitousDestinationFilter(
    @SerialName("i")
    val toId: String,
): DestinationFilter