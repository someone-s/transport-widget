package com.eden.livewidget.data.agency.common.point

import androidx.compose.runtime.Stable
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
@Stable
abstract class LogicalPoint {
    abstract val name: String
}