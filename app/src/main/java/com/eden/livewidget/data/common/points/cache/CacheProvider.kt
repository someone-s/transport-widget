package com.eden.livewidget.data.common.points.cache

import android.content.Context

interface CacheProvider {

    suspend fun getCache(context: Context): Cache

    suspend fun removeCache(context: Context)
}