package com.eden.livewidget.data.agency.common.text

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("l")
data class LiteralText(
    val value: String
): Text
