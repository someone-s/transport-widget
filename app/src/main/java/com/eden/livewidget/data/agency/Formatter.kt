package com.eden.livewidget.data.agency

import com.eden.livewidget.data.agency.common.point.DataPoint
import com.eden.livewidget.data.agency.common.destination.DestinationFilter
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.common.point.FloatLogicalPoint
import com.eden.livewidget.data.agency.common.point.LogicalPoint
import com.eden.livewidget.data.agency.rdg.point.RdgDataPoint
import com.eden.livewidget.data.agency.rdg.destination.RdgDestinationFilter
import com.eden.livewidget.data.agency.tfl.point.TflDataPoint
import com.eden.livewidget.data.agency.tfl.destination.TflDestinationFilter
import com.eden.livewidget.data.agency.trasitous.point.TransitousDataPoint
import com.eden.livewidget.data.agency.trasitous.destination.TransitousDestinationFilter
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val formatter = Json {
    serializersModule = SerializersModule {
        polymorphic(LogicalPoint::class) {
            subclass(FixedLogicalPoint::class)
            subclass(FloatLogicalPoint::class)
        }
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