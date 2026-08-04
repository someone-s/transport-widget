// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.arrivals

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


class ArrivalsDataSource(
    private val arrivalsApi: ArrivalsApi,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun fetchLatestArrivals(context: Context): Pair<FetchResult, List<ArrivalModel>> =
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            try {
                Pair(FetchResult.SUCCESS, arrivalsApi.fetchLatestArrivals(context))
            } catch (_: ArrivalsApi.Companion.UnreachableException) {
                Pair(FetchResult.ERROR_UNREACHABLE, emptyList())
            } catch (_: ArrivalsApi.Companion.AuthenticationException) {
                Pair(FetchResult.ERROR_AUTHENTICATION, emptyList())
            }
        }

    companion object {
        enum class FetchResult { SUCCESS, ERROR_UNREACHABLE, ERROR_AUTHENTICATION }
    }
}
