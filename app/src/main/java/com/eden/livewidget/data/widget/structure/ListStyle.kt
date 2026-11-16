package com.eden.livewidget.data.widget.structure

import kotlinx.serialization.Serializable

@Serializable
enum class ListStyle {
    SHOW_LATER_FULL,
    SHOW_LATER_GROUPED,
    HIDE_LATER,
}