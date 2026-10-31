package com.eden.livewidget.data.tfl

import android.net.Uri
import com.eden.livewidget.data.common.arrivals.LocationSupplement
import com.eden.livewidget.data.common.arrivals.ArrivalEntry
import java.time.LocalDateTime

class TflArrivalEntry(
    operatorName: String,
    serviceName: String,
    destinationName: String,
    locationSupplement: LocationSupplement?,
    platformName: String,
    remainingS: Int,
    expectedDateTime: LocalDateTime,
    detailUri: Uri,
    val modeName: String,
    val lineId: String,
    val direction: String,
    val finalNaptanId: String,
): ArrivalEntry(
    operatorName = operatorName,
    serviceName = serviceName,
    destinationName = destinationName,
    locationSupplement = locationSupplement,
    platformName = platformName,
    remainingS = remainingS,
    expectedDateTime = expectedDateTime,
    detailUri = detailUri,
)