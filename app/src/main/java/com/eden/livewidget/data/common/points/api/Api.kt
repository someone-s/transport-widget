package com.eden.livewidget.data.common.points.api

import android.content.Context
import com.eden.livewidget.data.common.points.Model
import kotlinx.coroutines.flow.Flow

interface Api {
    suspend fun fetchPoints(
        context: Context,
        statusUpdate: (status: String) -> Unit,
    ): Flow<Model>
}