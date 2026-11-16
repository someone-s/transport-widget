package com.eden.livewidget.data.agency.common.arrivals

import android.net.Uri
import java.time.LocalDateTime

open class ArrivalEntry(
    val operatorName: String,
    val serviceName: String,
    val destinationName: String,
    val locationPretext: LocationPretext? = null,
    val locationSupplement: LocationSupplement? = null,
    val platformName: String? = null,
    val remainingS: Int,
    val expectedDateTime: LocalDateTime,
    val detailUri: Uri? = null,
)

sealed interface LocationPretext

sealed interface LocationSupplement

data class LocationVia(
    val viaText: String,
): LocationSupplement

data class LocationFrom(
    val fromText: String,
): LocationSupplement

data class LocationBoard(
    val boardText: String,
): LocationPretext