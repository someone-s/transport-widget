package com.eden.livewidget.data.common.arrivals.filter

import com.eden.livewidget.data.common.arrivals.filter.destination.Compiler as DestinationCompiler

data class Constructors(
    val destinationCompilerConstructor: () -> DestinationCompiler,
)