package com.eden.livewidget.data.agency.common.arrivals

import com.eden.livewidget.data.agency.common.FetchResult
import java.time.LocalDateTime

data class ArrivalsState(
    val lastUpdate: LocalDateTime? = null,
    val lastResult: FetchResult? = null,
    val validity: Validity = Validity.UNINITIALIZED,
    val lastValidData: List<ArrivalEntry> = emptyList()
) {
    enum class Validity { VALID, INVALID, UNINITIALIZED, }

}