package com.eden.livewidget.data.widget.structure

import kotlinx.serialization.Serializable

@Serializable
data class DisplayOptions(
    val listStyle: ListStyle,
)

val defaultDisplayOption = DisplayOptions(
    listStyle = ListStyle.SHOW_LATER_FULL
)
