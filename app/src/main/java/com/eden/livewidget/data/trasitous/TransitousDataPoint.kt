package com.eden.livewidget.data.trasitous

import com.eden.livewidget.data.common.DataPoint
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
