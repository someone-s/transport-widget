package com.eden.livewidget.data.rdg

import android.content.Context
import com.eden.livewidget.data.buffered.BufferedPointsRepository
import com.eden.livewidget.data.keys.ObscuredKeyProvider
import com.eden.livewidget.data.common.ArrivalsRepository
import com.eden.livewidget.data.common.DestinationCompiler
import com.eden.livewidget.data.common.PointsRepository
import com.eden.livewidget.data.common.Provider
import com.eden.livewidget.data.keys.Key
import com.eden.livewidget.data.keys.KeyPurpose
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
            api = RdgPointsApi(context, keys.first { it.purposes.contains(KeyPurpose.POINTS) }.provider),
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