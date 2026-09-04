package com.eden.livewidget.data.common.points.cache

import com.eden.livewidget.data.common.points.Option

interface Cache {

    fun getAllFuzzyMatches(search: String): List<Option>

    fun insert(option: Option)

    fun deleteAll()
}