// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.common.arrivals

import android.content.Context
import com.eden.livewidget.data.common.arrivals.api.Api
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext


class ArrivalsDataSource(
    private val api: Api,
    private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun fetchLatestArrivals(context: Context): Pair<FetchResult, List<ArrivalModel>> =
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            try {
                Pair(FetchResult.SUCCESS, api.fetchLatestArrivals(context))
            } catch (_: Api.UnresolvedException) {
                Pair(FetchResult.ERROR_UNRESOLVED, emptyList())
            } catch (_: Api.UnreachableException) {
                Pair(FetchResult.ERROR_UNREACHABLE, emptyList())
            } catch (_: Api.AuthenticationException) {
                Pair(FetchResult.ERROR_AUTHENTICATION, emptyList())
            }
        }

    enum class FetchResult { SUCCESS, ERROR_UNRESOLVED, ERROR_UNREACHABLE, ERROR_AUTHENTICATION }
}
