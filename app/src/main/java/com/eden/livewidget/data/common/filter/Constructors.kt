package com.eden.livewidget.data.common.filter

import com.eden.livewidget.data.common.filter.destination.DestinationCompiler

data class Constructors(
    val destinationCompilerConstructor: () -> DestinationCompiler,
)