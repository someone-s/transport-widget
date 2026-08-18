package com.eden.livewidget.data.rdg.points

import com.eden.livewidget.data.common.points.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("rdg")
class RdgValue(
    @SerialName("c")
    val crsCode: String,
): Value {

    override val displayString: String = crsCode

}