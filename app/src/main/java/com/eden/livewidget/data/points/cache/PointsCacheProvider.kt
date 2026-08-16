package com.eden.livewidget.data.points.cache

import android.content.Context

interface PointsCacheProvider {

    suspend fun getCache(context: Context): PointsCache

    suspend fun removeCache(context: Context)
}