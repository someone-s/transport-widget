package com.eden.livewidget.data.common

import com.eden.livewidget.data.keys.Key
import com.eden.livewidget.data.keys.KeyProvider

interface Provider {
    val keys: List<Key>
    val pointsRepository: PointsRepository
    val arrivalsRepository: ArrivalsRepository
    val destinationCompiler: DestinationCompiler
}