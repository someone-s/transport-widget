package com.eden.livewidget.data.points.cache

import com.eden.livewidget.data.points.PointModel

interface PointsCache {

    fun getAllFuzzyMatches(search: String): List<PointModel>

    fun insert(point: PointModel)

    fun deleteAll()
}