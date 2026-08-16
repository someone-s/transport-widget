package com.eden.livewidget.data.common.points.cache

import com.eden.livewidget.data.common.points.Model

interface Cache {

    fun getAllFuzzyMatches(search: String): List<Model>

    fun insert(point: Model)

    fun deleteAll()
}