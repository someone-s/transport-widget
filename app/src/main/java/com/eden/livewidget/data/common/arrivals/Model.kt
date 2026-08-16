package com.eden.livewidget.data.common.arrivals

import java.time.LocalDateTime


data class Model(
    val operatorName: String,
    val serviceName: String,
    val destinationName: String,
    val viaText: String = "",
    val platformName: String,
    val remainingS: Int,
    val expectedDateTime: LocalDateTime
)