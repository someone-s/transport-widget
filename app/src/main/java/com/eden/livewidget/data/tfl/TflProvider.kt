package com.eden.livewidget.data.tfl

import android.content.Context
import com.eden.livewidget.data.buffered.BufferedPointsRepository
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.DestinationCompiler
import com.eden.livewidget.data.common.PointsRepository
import com.eden.livewidget.data.common.Provider
import com.eden.livewidget.data.keys.Key

class TflProvider(
    private val context: Context,
): Provider {
    override val keys: List<Key> = emptyList()

    override val pointsRepository: PointsRepository by lazy {
        BufferedPointsRepository(
            api = TflPointsApi(context),
            cache = TflPointsCache(context),
        )
    }
    override val arrivalsRepository: ArrivalsRepository by lazy {
        TflArrivalsRepository(
            api = TflArrivalsApi(),
        )
    }
    override val destinationCompiler: DestinationCompiler by lazy {
        TflDestinationCompiler()
    }
}