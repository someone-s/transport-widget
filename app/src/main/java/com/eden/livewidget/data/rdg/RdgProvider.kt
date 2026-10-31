package com.eden.livewidget.data.rdg

import android.content.Context
import com.eden.livewidget.data.buffered.BufferedPointsRepository
import com.eden.livewidget.data.keys.KeyProvider
import com.eden.livewidget.data.keys.ObscuredKeyProvider
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.PointsRepository
import com.eden.livewidget.data.common.Provider

class RdgProvider(
    context: Context
) : Provider {

    private val pointsKeyProvider: KeyProvider by lazy {
        ObscuredKeyProvider($"RDG-POINTS")
    }

    override val pointsRepository: PointsRepository by lazy {
        BufferedPointsRepository(
            api = RdgPointsApi(context, pointsKeyProvider),
            cache = RdgPointsCache(context),
        )
    }

    private val arrivalsKeyProvider: KeyProvider by lazy {
        ObscuredKeyProvider($"RDG-ARRIVALS")
    }

    override val arrivalsRepository: ArrivalsRepository by lazy {
        RdgArrivalsRepository(
            api = RdgArrivalsApi(context, arrivalsKeyProvider),
        )
    }
}