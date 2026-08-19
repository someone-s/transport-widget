package com.eden.livewidget.data.common.points

import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val name: String,
    val values: List<Value>, // api specific value
) {
    constructor(
        name: String,
        value: Value,
    ): this(
        name = name,
        values = listOf(value)
    )
}