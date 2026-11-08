package com.eden.livewidget.data.agency.common.text

import androidx.annotation.StringRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("r")
data class ResourceText(
    @StringRes
    val value: Int
): Text