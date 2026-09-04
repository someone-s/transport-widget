package com.eden.livewidget.data.common.points.api

import android.content.Context
import com.eden.livewidget.data.common.points.Option

interface DirectApi {
    suspend fun getAllFuzzyMatches(
        context: Context,
        input: String,
    ): List<Option>
}