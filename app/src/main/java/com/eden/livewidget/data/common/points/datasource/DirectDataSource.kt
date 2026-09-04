package com.eden.livewidget.data.common.points.datasource

import android.content.Context
import com.eden.livewidget.data.common.points.Option
import com.eden.livewidget.data.common.points.api.DirectApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DirectDataSource(
    private val pointsDirectApi: DirectApi,
    private val ioDispatcher: CoroutineDispatcher,
): DataSource {

    override suspend fun fetchMatching(
        context: Context,
        input: String
    ): List<Option> =
        withContext(ioDispatcher) {
            pointsDirectApi.getAllFuzzyMatches(context, input)
        }
}