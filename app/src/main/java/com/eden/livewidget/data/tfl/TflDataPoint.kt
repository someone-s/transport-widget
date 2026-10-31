package com.eden.livewidget.data.tfl

import com.eden.livewidget.data.common.DataPoint
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