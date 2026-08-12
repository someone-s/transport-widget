package com.eden.livewidget.data.arrivals

import java.time.LocalDateTime

data class ArrivalsData(
    val lastUpdate: LocalDateTime? = null,
    val validity: Validity = Validity.INVALID,
    val lastValidData: List<ArrivalModel> = emptyList()
) {
    enum class Validity { VALID, INVALID }

}