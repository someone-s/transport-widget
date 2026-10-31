package com.eden.livewidget.data.common

interface Provider {
    val pointsRepository: PointsRepository
    val arrivalsRepository: ArrivalsRepository
    val destinationCompiler: DestinationCompiler
}