package com.eden.livewidget.data.agency.trasitous

import android.content.Context
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.point.PointsRepository
import com.eden.livewidget.data.agency.common.Provider
import com.eden.livewidget.data.agency.common.keys.Key
import com.eden.livewidget.data.agency.trasitous.arrivals.TransitousArrivalsApi
import com.eden.livewidget.data.agency.trasitous.arrivals.TransitousArrivalsRepository
import com.eden.livewidget.data.agency.trasitous.destination.TransitousDestinationCompiler
import com.eden.livewidget.data.agency.trasitous.point.TransitousPointsApi
import com.eden.livewidget.data.agency.trasitous.point.TransitousPointsRepository

class TransitousProvider(
    private val context: Context,
): Provider {
    override val keys: List<Key> = emptyList()

    override val pointsRepository: PointsRepository by lazy {
        TransitousPointsRepository(
            api = TransitousPointsApi(),
        )
    }
    override val arrivalsRepository: ArrivalsRepository by lazy {
        TransitousArrivalsRepository(
            api = TransitousArrivalsApi(),
        )
    }
    override val destinationCompiler: DestinationCompiler by lazy {
        TransitousDestinationCompiler()
    }
}