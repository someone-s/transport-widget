package com.eden.livewidget.data.common.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.filter.Status
import com.eden.livewidget.data.common.points.Model
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val fromPoint: Model,
    val toPoint: Model,
    val values: List<Value>?,
    val status: Status,
) {
    companion object {
        fun createPending(
            fromPoint: Model,
            toPoint: Model,
        ) = Filter(
            fromPoint = fromPoint,
            toPoint = toPoint,
            values = null,
            status = Status.PENDING,
        )

        fun Filter.cloneApplied(
            values: List<Value>,
        ) = Filter(
            fromPoint = this.fromPoint,
            toPoint = this.toPoint,
            values = values,
            status = Status.APPLIED,
        )
    }
}