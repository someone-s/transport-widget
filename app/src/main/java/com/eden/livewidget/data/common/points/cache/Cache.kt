package com.eden.livewidget.data.common.points.cache

import com.eden.livewidget.data.common.points.PointModel

interface Cache {

    fun getAllFuzzyMatches(search: String): List<PointModel>

    fun insert(point: PointModel)

    fun deleteAll()
}