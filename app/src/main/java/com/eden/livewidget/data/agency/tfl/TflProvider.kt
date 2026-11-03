package com.eden.livewidget.data.agency.tfl

import android.content.Context
import com.eden.livewidget.data.agency.buffered.point.BufferedPointsRepository
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.point.PointsRepository
import com.eden.livewidget.data.agency.common.Provider
import com.eden.livewidget.data.agency.common.keys.Key
import com.eden.livewidget.data.agency.tfl.arrivals.TflArrivalsApi
import com.eden.livewidget.data.agency.tfl.arrivals.TflArrivalsRepository
import com.eden.livewidget.data.agency.tfl.destination.TflDestinationCompiler
import com.eden.livewidget.data.agency.tfl.point.TflPointsApi
import com.eden.livewidget.data.agency.tfl.point.TflPointsCache

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