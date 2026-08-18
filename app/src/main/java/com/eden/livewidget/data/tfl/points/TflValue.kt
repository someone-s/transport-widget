package com.eden.livewidget.data.tfl.points

import com.eden.livewidget.data.common.points.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("tfl")
class TflValue(

    @SerialName("n")
    val naptanId: String,

    @SerialName("l")
    val lineIds: List<String>,

): Value {

    override val displayString: String = naptanId

}