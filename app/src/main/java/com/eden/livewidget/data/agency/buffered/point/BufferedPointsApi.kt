package com.eden.livewidget.data.agency.buffered.point

import kotlinx.coroutines.flow.Flow

interface BufferedPointsApi {
    suspend fun fetchPoints(statusUpdate: ((status: String) -> Unit)? = null): Flow<BufferedPointEntry>
}