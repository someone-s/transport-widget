package com.eden.livewidget.data.buffered

import kotlinx.coroutines.flow.Flow

interface BufferedPointsApi {
    suspend fun fetchPoints(statusUpdate: ((status: String) -> Unit)? = null): Flow<BufferedPointOption>
}