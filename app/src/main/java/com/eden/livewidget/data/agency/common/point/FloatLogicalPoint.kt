package com.eden.livewidget.data.agency.common.point

import androidx.compose.runtime.Stable
import com.eden.livewidget.data.agency.common.text.Text
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("l-float")
@Stable
open class FloatLogicalPoint(override val name: Text) : LogicalPoint()