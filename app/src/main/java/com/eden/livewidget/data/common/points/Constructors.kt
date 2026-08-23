package com.eden.livewidget.data.common.points

import com.eden.livewidget.data.common.points.datasource.DataSource
import kotlin.reflect.KClass

data class Constructors(
    val dataSourceConstructor: () -> DataSource,
)