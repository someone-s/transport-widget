package com.eden.livewidget.data.common

import java.time.LocalDateTime

data class ArrivalsState(
    val lastUpdate: LocalDateTime? = null,
    val validity: Validity = Validity.UNINITIALIZED,
    val lastValidData: List<ArrivalEntry> = emptyList()
) {
    enum class Validity { VALID, INVALID, UNINITIALIZED, }

}