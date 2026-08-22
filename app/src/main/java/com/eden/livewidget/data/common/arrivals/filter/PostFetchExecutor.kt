package com.eden.livewidget.data.common.arrivals.filter

import com.eden.livewidget.data.common.arrivals.Model as ArrivalsModel

interface PostFetchExecutor {
    fun execute(arrivalsModels: List<ArrivalsModel>): List<ArrivalsModel>
}