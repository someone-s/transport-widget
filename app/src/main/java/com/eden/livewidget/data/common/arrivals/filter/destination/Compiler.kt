package com.eden.livewidget.data.common.arrivals.filter.destination

interface Compiler {
    suspend fun compileFilter(filter: Filter): Filter
}