package com.eden.livewidget.data.tfl.arrivals

import com.eden.livewidget.data.common.arrivals.Model
import java.time.LocalDateTime

class TflModel(
    operatorName: String,
    serviceName: String,
    destinationName: String,
    viaText: String = "",
    platformName: String,
    remainingS: Int,
    expectedDateTime: LocalDateTime,
    val modeName: String,
    val lineId: String,
    val direction: String,
    val finalNaptanId: String,
): Model(
    operatorName = operatorName,
    serviceName = serviceName,
    destinationName = destinationName,
    viaText = viaText,
    platformName = platformName,
    remainingS = remainingS,
    expectedDateTime = expectedDateTime,
)