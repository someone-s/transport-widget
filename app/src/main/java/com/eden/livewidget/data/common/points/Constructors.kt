package com.eden.livewidget.data.common.points

import com.eden.livewidget.data.common.points.datasource.DataSource

data class Constructors(
    val dataSourceConstructor: () -> DataSource,
)