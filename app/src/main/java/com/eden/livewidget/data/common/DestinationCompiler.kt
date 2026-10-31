package com.eden.livewidget.data.common

interface DestinationCompiler {
    suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter>
}