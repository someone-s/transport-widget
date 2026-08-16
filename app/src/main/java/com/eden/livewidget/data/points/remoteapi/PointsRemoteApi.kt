package com.eden.livewidget.data.points.remoteapi

import android.content.Context
import com.eden.livewidget.data.points.PointModel
import kotlinx.coroutines.flow.Flow

interface PointsRemoteApi {
    suspend fun fetchPoints(
        context: Context,
        statusUpdate: (status: String) -> Unit,
    ): Flow<PointModel>
}