package com.eden.livewidget.data.tfl.arrivals.filter.destination

import com.eden.livewidget.data.common.arrivals.Model as ArrivalsModel
import com.eden.livewidget.data.common.arrivals.filter.PostFetchExecutor
import com.eden.livewidget.data.tfl.arrivals.TflModel

class TflPostFetchExecutor(
    val values: List<TflValue>
): PostFetchExecutor {
    override fun execute(arrivalsModels: List<ArrivalsModel>): List<ArrivalsModel> =
        if (values.isEmpty())
            arrivalsModels
        else
            arrivalsModels
                .map { it as TflModel }
                .filter { pointsValue ->
                    values
                        .map { value -> Pair(value.lineId, value.direction) }
                        .contains(Pair(pointsValue.lineId, pointsValue.direction))
                }
}