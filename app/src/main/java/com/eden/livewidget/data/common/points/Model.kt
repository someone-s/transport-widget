package com.eden.livewidget.data.common.points

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