package com.eden.livewidget.data.agency.trasitous.point

import com.eden.livewidget.data.agency.common.point.DataPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("p-t00")
open class TransitousDataPoint(
    @SerialName("i")
    val id: String,
): DataPoint {

    override val displayString: String = id
}
