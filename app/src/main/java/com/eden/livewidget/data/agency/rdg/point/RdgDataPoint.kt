package com.eden.livewidget.data.agency.rdg.point

import com.eden.livewidget.data.agency.common.point.DataPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("p-rdg")
open class RdgDataPoint(
    @SerialName("c")
    val crsCode: String,
): DataPoint {

    override val displayString: String = crsCode

}