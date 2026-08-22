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
        else {
            val lineDirectionPairs = values.map { value -> Pair(value.lineId, value.direction) }
            val lineFinalPairs = values.flatMap { value -> value.finalNaptanIds.map { Pair(value.lineId, it) } }
            arrivalsModels
                .map { it as TflModel }
                .filter { arrivalModel ->
                    if (arrivalModel.finalNaptanId != "")
                        lineFinalPairs.contains(Pair(arrivalModel.lineId, arrivalModel.finalNaptanId))
                    else
                        lineDirectionPairs.contains(Pair(arrivalModel.lineId, arrivalModel.direction))
                }
        }
}