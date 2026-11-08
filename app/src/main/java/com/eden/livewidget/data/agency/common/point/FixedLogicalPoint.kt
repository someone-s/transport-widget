package com.eden.livewidget.data.agency.common.point

import androidx.compose.runtime.Stable
import com.eden.livewidget.data.agency.common.text.LiteralText
import com.eden.livewidget.data.agency.common.text.Text
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("l-fixed")
@Stable
data class FixedLogicalPoint(
    val nameString: String,
    val dataPoints: List<DataPoint>, // api specific value
): LogicalPoint() {
    override val name: Text = LiteralText(nameString)

    constructor(
        nameString: String,
        dataPoint: DataPoint,
    ): this(
        nameString = nameString,
        dataPoints = listOf(dataPoint)
    )
}