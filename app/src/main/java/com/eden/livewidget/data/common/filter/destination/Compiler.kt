package com.eden.livewidget.data.common.filter.destination

typealias DestinationCompiler = Compiler
interface Compiler {
    suspend fun compileFilter(filter: Filter): Filter
}