package com.eden.livewidget.data.common

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface PointsRepository {
    val matchingPoints: Flow<List<Option>>

    data class Option(
        val logicalPoint: LogicalPoint,
        val annotation: String? = null,
    )

    suspend fun fetchMatching(input: String)

    val asRefreshable: Refreshable?
        get() = this as? Refreshable

    interface Refreshable {
        suspend fun refresh(statusUpdate: ((status: String) -> Unit)? = null)
    }


    val asResettable: Resettable?
        get() = this as? Resettable

    interface Resettable {
        suspend fun reset(context: Context)
    }

}