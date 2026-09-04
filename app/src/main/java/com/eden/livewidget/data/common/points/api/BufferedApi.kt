package com.eden.livewidget.data.common.points.api

import android.content.Context
import com.eden.livewidget.data.common.points.Option
import kotlinx.coroutines.flow.Flow

interface BufferedApi {
    suspend fun fetchPoints(
        context: Context,
        statusUpdate: (status: String) -> Unit,
    ): Flow<Option>
}