package com.eden.livewidget.data.transitous.points

import com.eden.livewidget.data.common.points.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("p-t00")
class TransitousValue(
    @SerialName("i")
    val id: String,
): Value {

    override val displayString: String = id
}