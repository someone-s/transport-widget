package com.eden.livewidget.data.tfl

import android.content.Context
import com.eden.livewidget.data.buffered.BufferedPointsRepository
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.PointsRepository
import com.eden.livewidget.data.common.Provider

class TflProvider(
    private val context: Context,
): Provider {
    override val pointsRepository: PointsRepository by lazy {
        BufferedPointsRepository(
            api = TflPointsApi(context),
            cache = TflPointsCache(context),
        )
    }
    override val arrivalsRepository: ArrivalsRepository by lazy {
        TflArrivalsRepository(
            api = TflArrivalsApi(context),
        )
    }
}