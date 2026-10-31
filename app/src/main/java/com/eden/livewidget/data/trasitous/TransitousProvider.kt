package com.eden.livewidget.data.trasitous

import android.content.Context
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.PointsRepository
import com.eden.livewidget.data.common.Provider
import com.eden.livewidget.data.tfl.TflArrivalsApi
import com.eden.livewidget.data.tfl.TflArrivalsRepository

class TransitousProvider(
    private val context: Context,
): Provider {
    override val pointsRepository: PointsRepository by lazy {
        TransitousPointsRepository(
            api = TransitousPointsApi(context),
        )
    }
    override val arrivalsRepository: ArrivalsRepository by lazy {
        TflArrivalsRepository(
            api = TflArrivalsApi(context),
        )
    }
}