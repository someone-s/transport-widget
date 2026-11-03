package com.eden.livewidget.data.agency.tfl.point

import com.eden.livewidget.data.agency.common.point.DataPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("p-tfl")
class TflDataPoint(

    @SerialName("n")
    val naptanId: String,

    @SerialName("l")
    val lineIds: List<String>,

): DataPoint {

    override val displayString: String = naptanId

}