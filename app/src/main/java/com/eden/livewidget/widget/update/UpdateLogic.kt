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
import com.eden.livewidget.data.pointsFormat
import com.eden.livewidget.data.common.filter.State as FilterState
import com.eden.livewidget.data.common.filter.emptyState as emptyFilterState
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
        pointsFormat.decodeFromString(valuesString)
    } catch (_: Exception) {
        return UpdateResult.ERROR_UNKNOWN
    }

    val filterStateString = preferences[LivePointWidget.FILTER_STATE_KEY]
    val filterState = if (filterStateString == null) emptyFilterState() else FilterState.deserialize(filterStateString)

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
            Repository.FetchResult.ERROR_UNREACHABLE -> UpdateResult.ERROR_UNREACHABLE
            Repository.FetchResult.ERROR_UNRESOLVED -> identifyMeteredNetworkIssue(context)
        }

    } catch (e: Exception) {
        Log.e(context.packageName, e.message ?: "Failed with no message", e)

        return identifyBatteryOptimizationIssue(context)
    }
}

private fun identifyMeteredNetworkIssue(context: Context): UpdateResult {
    val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        ?: return UpdateResult.ERROR_UNKNOWN

    connectivityManager.apply {
        return if (isActiveNetworkMetered && restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED)
            UpdateResult.ERROR_METERED
        else
            UpdateResult.ERROR_UNRESOLVED
    }
}

private fun identifyBatteryOptimizationIssue(context: Context): UpdateResult {
    val powerManager = (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)
        ?: return UpdateResult.ERROR_UNKNOWN

    powerManager.apply {
        return if (isPowerSaveMode && isIgnoringBatteryOptimizations(context.packageName))
            UpdateResult.ERROR_BATTERY
        else
            UpdateResult.ERROR_UNKNOWN
    }
}