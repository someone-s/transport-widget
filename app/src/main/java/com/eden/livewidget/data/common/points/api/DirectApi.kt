package com.eden.livewidget.data.common.points.api

import android.content.Context
import com.eden.livewidget.data.common.points.Model

interface DirectApi {
    suspend fun getAllFuzzyMatches(
        context: Context,
        input: String,
    ): List<Model>
}