package com.eden.livewidget.data.common.arrivals

import java.time.LocalDateTime

data class Data(
    val lastUpdate: LocalDateTime? = null,
    val validity: Validity = Validity.UNINITIALIZED,
    val lastValidData: List<Model> = emptyList()
) {
    enum class Validity { VALID, INVALID, UNINITIALIZED, }

}