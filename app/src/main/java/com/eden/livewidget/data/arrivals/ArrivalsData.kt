package com.eden.livewidget.data.arrivals

data class ArrivalsData(
    val validity: Validity = Validity.INVALID,
    val lastValidData: List<ArrivalModel> = emptyList()
) {
    enum class Validity { VALID, INVALID }

}