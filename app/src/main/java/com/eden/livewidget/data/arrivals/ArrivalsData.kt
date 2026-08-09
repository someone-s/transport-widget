package com.eden.livewidget.data.arrivals

data class ArrivalsData(
    val validity: Validity = Validity.INVALID_UNINITIALIZED,
    val lastValidData: List<ArrivalModel> = emptyList()
) {
    enum class Validity { VALID, INVALID_AUTHENTICATION, INVALID_UNREACHABLE, INVALID_UNRESOLVED, INVALID_UNINITIALIZED }

}