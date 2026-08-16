package com.eden.livewidget.data.common.filter.destination

import com.eden.livewidget.data.common.filter.Status
import com.eden.livewidget.data.common.points.Model

data class Filter(
    val fromPoint: Model,
    val toPoint: Model,
    val filterValue: String?,
    val status: Status,
) {
    companion object {
        fun createPending(
            fromPoint: Model,
            toPoint: Model,
        ) = Filter(
            fromPoint = fromPoint,
            toPoint = toPoint,
            filterValue = null,
            status = Status.PENDING,
        )

        fun Filter.cloneApplied(
            filterValue: String,
        ) = Filter(
            fromPoint = this.fromPoint,
            toPoint = this.toPoint,
            filterValue = filterValue,
            status = Status.APPLIED,
        )
    }
}