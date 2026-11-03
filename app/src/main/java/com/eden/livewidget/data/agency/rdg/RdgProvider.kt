package com.eden.livewidget.data.agency.rdg

import android.content.Context
import com.eden.livewidget.data.agency.buffered.point.BufferedPointsRepository
import com.eden.livewidget.data.agency.common.keys.ObscuredKeyProvider
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsRepository
import com.eden.livewidget.data.agency.common.destination.DestinationCompiler
import com.eden.livewidget.data.agency.common.point.PointsRepository
import com.eden.livewidget.data.agency.common.Provider
import com.eden.livewidget.data.agency.common.keys.Key
import com.eden.livewidget.data.agency.common.keys.KeyPurpose
import com.eden.livewidget.data.agency.rdg.arrivals.RdgArrivalsApi
import com.eden.livewidget.data.agency.rdg.arrivals.RdgArrivalsRepository
import com.eden.livewidget.data.agency.rdg.destination.RdgDestinationCompiler
import com.eden.livewidget.data.agency.rdg.point.RdgPointsApi
import com.eden.livewidget.data.agency.rdg.point.RdgPointsCache
import java.util.EnumSet

class RdgProvider(
    context: Context
) : Provider {

    override val keys: List<Key> by lazy {
        listOf(
            Key(
                purposes = EnumSet.of(KeyPurpose.POINTS),
                provider = ObscuredKeyProvider(context, "RDG-POINTS"),
            ),
            Key(
                purposes = EnumSet.of(KeyPurpose.ARRIVALS),
                provider = ObscuredKeyProvider(context, "RDG-ARRIVALS"),
            )
        )
    }

    override val pointsRepository: PointsRepository by lazy {
        BufferedPointsRepository(
            api = RdgPointsApi(
                context,
                keys.first { it.purposes.contains(KeyPurpose.POINTS) }.provider
            ),
            cache = RdgPointsCache(context),
        )
    }


    override val arrivalsRepository: ArrivalsRepository by lazy {
        RdgArrivalsRepository(
            api = RdgArrivalsApi(keys.first { it.purposes.contains(KeyPurpose.ARRIVALS) }.provider),
        )
    }
    override val destinationCompiler: DestinationCompiler by lazy {
        RdgDestinationCompiler()
    }

}