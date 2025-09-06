package com.eden.livewidget.data.points

import com.eden.livewidget.data.Provider

data class PointModel(
    val name: String,
    val apiProvider: Provider,
    val apiValue: String, // api specific format id
    val context: String? = null,
)