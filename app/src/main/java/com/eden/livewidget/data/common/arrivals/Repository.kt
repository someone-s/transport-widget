// See: https://developer.android.com/topic/architecture/data-layer
package com.eden.livewidget.data.common.arrivals

import android.content.Context
import android.util.Log
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.arrivals.filter.State
import com.eden.livewidget.data.common.arrivals.filter.PostFetchExecutor
import com.eden.livewidget.data.common.arrivals.filter.PreFetchExecutor
import com.eden.livewidget.data.common.points.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import java.time.LocalDateTime

class Repository(
    private val dataSource: DataSource,
    private val values: List<Value>,
    private val preFetchExecutors: List<PreFetchExecutor>,
    private val postFetchExecutors: List<PostFetchExecutor>,
) {

    private val arrivalsDataMutable = MutableStateFlow(Data())
    val arrivalsData = arrivalsDataMutable.asStateFlow()

    suspend fun fetchLatestArrival(context: Context): FetchResult {


        val filteredValues = values.run {
            var intermediateValues = this
            for (executor in preFetchExecutors)
                intermediateValues = executor.execute(intermediateValues)
            intermediateValues
        }
        val output = dataSource.fetchLatestArrivals(context, filteredValues)

        Log.i(javaClass.name, "${output.first}")

        arrivalsDataMutable.getAndUpdate { previousData ->
            when (output.first) {
                DataSource.FetchResult.SUCCESS -> {
                    val results = output.second
                    val filteredResults = results.run {
                        var intermediateResults = this
                        for (executor in postFetchExecutors)
                            intermediateResults = executor.execute(intermediateResults)
                        intermediateResults
                    }
                    Data(LocalDateTime.now(), Data.Validity.VALID, filteredResults)
                }
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
        ): Repository =
            instances.getOrPut(key) {
                val constructors = provider.arrivalsConstructors

                Repository(
                    dataSource = constructors.dataSourceConstructor(),
                    values = values,
                    preFetchExecutors = constructors.preFetchExecutorConstructor(filterState),
                    postFetchExecutors = constructors.postFetchExecutorConstructor(filterState),
                )
            }
    }
}