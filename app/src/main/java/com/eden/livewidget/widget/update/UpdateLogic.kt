package com.eden.livewidget.widget.update

import android.content.Context
import android.net.ConnectivityManager
import android.os.PowerManager
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.eden.livewidget.agencyFromString
import com.eden.livewidget.data.common.arrivals.Repository
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.format
import com.eden.livewidget.data.common.arrivals.filter.emptyState as emptyFilterState
import com.eden.livewidget.widget.LivePointWidget

enum class UpdateResult(val widgetValue: String) {
    ERROR_UNKNOWN(LivePointWidget.FETCH_RESULT_ERROR_UNKNOWN),
    ERROR_AUTHENTICATION(LivePointWidget.FETCH_RESULT_ERROR_AUTHENTICATION),
    ERROR_UNREACHABLE(LivePointWidget.FETCH_RESULT_ERROR_UNREACHABLE),
    ERROR_UNRESOLVED(LivePointWidget.FETCH_RESULT_ERROR_UNRESOLVED),
    ERROR_METERED(LivePointWidget.FETCH_RESULT_ERROR_METERED),
    ERROR_BATTERY(LivePointWidget.FETCH_RESULT_ERROR_BATTERY),
    RAN_SKIPPED(LivePointWidget.FETCH_RESULT_RAN_SKIPPED),
    RAN_COMPLETED(LivePointWidget.FETCH_RESULT_RAN_COMPLETED),
}
suspend fun updateWidget(
    context: Context,
    appWidgetId: Int,
    remainingTimes: Int
): Boolean {

    val manager = GlanceAppWidgetManager(context)
    // if illegal exception let worker fail
    val glanceId = manager.getGlanceIdBy(appWidgetId)

    // Glance use this instance to generate RemoteView to show
    val updater = LivePointWidget()

    val result =
        if (remainingTimes < 0)
            UpdateResult.RAN_SKIPPED
        else
            updateData(context, glanceId)

    updateAppWidgetState(context, glanceId) { preferences ->
        preferences[LivePointWidget.FETCH_STATE_KEY] = result.widgetValue
    }

    updater.update(context, glanceId)

    return when (result) {
        UpdateResult.RAN_COMPLETED,
        UpdateResult.RAN_SKIPPED -> true
        else -> false
    }
}

private suspend fun updateData(
    context: Context,
    glanceId: GlanceId
): UpdateResult {

    // PreferencesGlanceStateDefinition is the default state definition used
    val preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

    val agencyString = preferences[LivePointWidget.AGENCY_KEY]
    val agency = try {
        checkNotNull(agencyString)
        agencyFromString(agencyString)!!
    } catch (_: Exception) {
        return UpdateResult.ERROR_UNKNOWN
    }

    val valuesString = preferences[LivePointWidget.VALUES_KEY]
    val values: List<Value> = try {
        checkNotNull(valuesString)
        format.decodeFromString(valuesString)
    } catch (_: Exception) {
        return UpdateResult.ERROR_UNKNOWN
    }

    val filterStateString = preferences[LivePointWidget.FILTER_STATE_KEY]
    val filterState =
        if (filterStateString == null)
            emptyFilterState()
        else
            format.decodeFromString(filterStateString)

    try {
        // Update data source
        val repository = Repository.getInstance(
            key = Repository.Companion.Key(
                agencyString = agencyString,
                valuesString = valuesString,
                filterStateString = filterStateString ?: "",
            ),
            provider = agency.apiProvider,
            values = values,
            filterState = filterState
        )
        val result = repository.fetchLatestArrival(context)

        return when (result) {
            Repository.FetchResult.SUCCESS -> UpdateResult.RAN_COMPLETED
            Repository.FetchResult.ERROR_AUTHENTICATION -> UpdateResult.ERROR_AUTHENTICATION
            Repository.FetchResult.ERROR_UNREACHABLE -> identityIssue(
                context = context,
                currentResult = UpdateResult.ERROR_UNREACHABLE
            )
            Repository.FetchResult.ERROR_UNRESOLVED -> identityIssue(
                context = context,
                currentResult = UpdateResult.ERROR_UNRESOLVED
            )
        }

    } catch (e: Exception) {
        Log.e(context.packageName, e.message ?: "Failed with no message", e)

        return identityIssue(
            context = context,
            currentResult = UpdateResult.ERROR_UNKNOWN
        )
    }
}

private fun identityIssue(
    context: Context,
    currentResult: UpdateResult,
): UpdateResult =
    UpdateResultBuilder(
        context = context,
        storedResult = currentResult,
    )
    .tryIdentifyMeteredNetworkIssue()
    .tryIdentifyBatteryOptimizationIssue()
    .build()

private data class UpdateResultBuilder(
    private val context: Context,
    private val storedResult: UpdateResult = UpdateResult.ERROR_UNKNOWN,
) {
    fun build(): UpdateResult = storedResult

    fun tryIdentifyMeteredNetworkIssue(): UpdateResultBuilder {

        val currentBuilder = this

        val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
            ?: return currentBuilder

        connectivityManager.apply {
            return if (isActiveNetworkMetered && restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED)
                UpdateResultBuilder(context, UpdateResult.ERROR_METERED)
            else
                currentBuilder
        }
    }

    fun tryIdentifyBatteryOptimizationIssue(): UpdateResultBuilder {
        val currentBuilder = this

        val powerManager = (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)
            ?: return currentBuilder

        powerManager.apply {
            return if (isPowerSaveMode && isIgnoringBatteryOptimizations(context.packageName))
                currentBuilder
            else
                UpdateResultBuilder(context, UpdateResult.ERROR_BATTERY)
        }
    }
}

