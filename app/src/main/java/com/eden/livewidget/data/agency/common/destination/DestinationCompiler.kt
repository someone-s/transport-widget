package com.eden.livewidget.data.agency.common.destination

interface DestinationCompiler {
    suspend fun compileFilter(filter: DestinationConfig): List<DestinationFilter>
}