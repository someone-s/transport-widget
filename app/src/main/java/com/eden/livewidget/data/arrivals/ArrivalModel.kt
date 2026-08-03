package com.eden.livewidget.data.arrivals

import java.time.LocalDateTime


data class ArrivalModel(
    val operatorName: String,
    val serviceName: String,
    val destinationName: String,
    val viaText: String = "",
    val platformName: String,
    val remainingS: Int,
    val expectedDateTime: LocalDateTime
)