package com.eden.livewidget.data.agency.common.point

import android.content.Context
import com.eden.livewidget.data.agency.common.FetchResult
import com.eden.livewidget.data.agency.common.text.Text
import kotlinx.coroutines.flow.Flow

interface PointsRepository {
    val matchingOptions: Flow<List<Option>>

    data class Option(
        val logicalPoint: LogicalPoint,
        val annotation: Text? = null,
    )

    suspend fun fetchMatching(input: String)

    val asLocationAware: LocationAware?
        get() = this as? LocationAware

    interface LocationAware {
        suspend fun getClosest(latitude: Double, longitude: Double): Pair<FetchResult, FixedLogicalPoint?>
    }

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