package com.eden.livewidget.data.agency.common

import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.point.PointsRepository
import com.eden.livewidget.data.agency.common.keys.Key

interface Provider {
    val keys: List<Key>
    val pointsRepository: PointsRepository
    val arrivalsRepository: ArrivalsRepository
    val destinationCompiler: DestinationCompiler
}