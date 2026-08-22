package com.eden.livewidget.data.common.arrivals

import com.eden.livewidget.data.common.arrivals.filter.State
import com.eden.livewidget.data.common.arrivals.filter.PostFetchExecutor as DestinationPostFetchExecutor
import com.eden.livewidget.data.common.arrivals.filter.PreFetchExecutor as DestinationPreFetchExecutor
import com.eden.livewidget.data.common.arrivals.filter.destination.Compiler as DestinationCompiler

data class Constructors(
    val dataSourceConstructor: () -> DataSource,
    val destinationCompilerConstructor: () -> DestinationCompiler,
    val preFetchExecutorConstructor: (State) -> List<DestinationPreFetchExecutor>,
    val postFetchExecutorConstructor: (State) -> List<DestinationPostFetchExecutor>,
)