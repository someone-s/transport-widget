package com.eden.livewidget.data.rdg

import com.eden.livewidget.data.common.DataPoint
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