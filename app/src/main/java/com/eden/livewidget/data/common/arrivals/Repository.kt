// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.common.arrivals

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.filter.State
import com.eden.livewidget.data.common.points.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.time.LocalDateTime

class Repository(
    private val dataSource: DataSource,
    private val values: List<Value>,
    private val filterState: State,
) {

    private val arrivalsDataMutable = MutableStateFlow(Data())
    val arrivalsData = arrivalsDataMutable.asStateFlow()

    suspend fun fetchLatestArrival(context: Context): FetchResult {
        val output = dataSource.fetchLatestArrivals(context, values)

        Log.i(javaClass.name, "${output.first}")

        arrivalsDataMutable.getAndUpdate { previousData ->
            when (output.first) {
                DataSource.FetchResult.SUCCESS ->
                    Data(LocalDateTime.now(), Data.Validity.VALID, output.second)
                DataSource.FetchResult.ERROR_UNRESOLVED ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
                DataSource.FetchResult.ERROR_UNREACHABLE ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
                DataSource.FetchResult.ERROR_AUTHENTICATION ->
                    Data(LocalDateTime.now(), Data.Validity.INVALID, previousData.lastValidData)
            }
        }

        return when (output.first) {
            DataSource.FetchResult.SUCCESS -> FetchResult.SUCCESS
            DataSource.FetchResult.ERROR_UNRESOLVED -> FetchResult.ERROR_UNRESOLVED
            DataSource.FetchResult.ERROR_UNREACHABLE -> FetchResult.ERROR_UNREACHABLE
            DataSource.FetchResult.ERROR_AUTHENTICATION -> FetchResult.ERROR_AUTHENTICATION
        }
    }

    enum class FetchResult {
        SUCCESS,
        ERROR_UNRESOLVED,
        ERROR_UNREACHABLE,
        ERROR_AUTHENTICATION,
    }

    companion object {

        data class Key(
            val agencyString: String,
            val valuesString: String,
            val filterStateString: String,
        )

        private var instances: MutableMap<Key, Repository> = mutableMapOf()

        fun getInstance(
            key: Key,
            provider: Provider,
            values: List<Value>,
            filterState: State,
        ): Repository {
            if (!instances.contains(key)) {
                instances[key] = Repository(
                    DataSource(
                        provider.arrivalsApiConstructor(),
                        Dispatchers.IO
                    ),
                    values,
                    filterState,
                )
            }

            return instances[key] as Repository
        }
    }
}