package com.eden.livewidget.data.common.arrivals

import java.time.LocalDateTime

open class Model(
    val operatorName: String,
    val serviceName: String,
    val destinationName: String,
    val locationPretext: LocationPretext? = null,
    val locationSupplement: LocationSupplement? = null,
    val platformName: String? = null,
    val remainingS: Int,
    val expectedDateTime: LocalDateTime,
)

sealed interface LocationPretext

sealed interface LocationSupplement

class LocationVia(
    val viaText: String,
): LocationSupplement

class LocationFrom(
    val fromText: String,
): LocationSupplement

class LocationBoard(
    val boardText: String,
): LocationPretext