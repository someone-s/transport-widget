package com.eden.livewidget.data

import com.eden.livewidget.data.common.DataPoint
import com.eden.livewidget.data.common.DestinationFilter
import com.eden.livewidget.data.rdg.RdgDataPoint
import com.eden.livewidget.data.rdg.RdgDestinationFilter
import com.eden.livewidget.data.tfl.TflDataPoint
import com.eden.livewidget.data.tfl.TflDestinationFilter
import com.eden.livewidget.data.trasitous.TransitousDataPoint
import com.eden.livewidget.data.trasitous.TransitousDestinationFilter
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val formatter = Json {
    serializersModule = SerializersModule {
        polymorphic(DataPoint::class) {
            subclass(TflDataPoint::class)
            subclass(RdgDataPoint::class)
            subclass(TransitousDataPoint::class)
        }
        polymorphic(DestinationFilter::class) {
            subclass(TflDestinationFilter::class)
            subclass(RdgDestinationFilter::class)
            subclass(TransitousDestinationFilter::class)
        }
    }
}