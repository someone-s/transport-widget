package com.eden.livewidget.widget.update

import android.content.Context
import android.os.PowerManager
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.data.providerFromString
import com.eden.livewidget.widget.LivePointWidget
import kotlin.time.Duration.Companion.seconds

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

    if (remainingTimes < 0) {

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.FETCH_RESULT_KEY] =
                LivePointWidget.FETCH_RESULT_RAN_SKIPPED
        }

        // update one more cycle than end
        updater.update(context, glanceId)
    } else {

        // PreferencesGlanceStateDefinition is the default state definition used
        val preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)

        val apiProvider = providerFromString(preferences[LivePointWidget.AGENCY_KEY])
            ?: return false

        val apiValue = preferences[LivePointWidget.API_VALUE_KEY]
            ?: return false

        try {
            // Update data source
            val repository = ArrivalsRepository.getInstance(apiProvider, apiValue)
            repository.fetchLatestArrival(context)

        } catch (e: Exception) {
            Log.e(context.packageName, e.message ?: "Failed with no message", e)

            val powerService = context.getSystemService(Context.POWER_SERVICE)
            if (powerService == null)
                updateAppWidgetState(context, glanceId) { preferences ->
                    preferences[LivePointWidget.FETCH_RESULT_KEY] =
                        LivePointWidget.FETCH_RESULT_ERROR_UNKNOWN
                }
            else {
                val powerManager = powerService as PowerManager
                if (
                    powerManager.isPowerSaveMode &&
                    !powerManager.isIgnoringBatteryOptimizations(context.packageName)
                )
                    updateAppWidgetState(context, glanceId) { preferences ->
                        preferences[LivePointWidget.FETCH_RESULT_KEY] =
                            LivePointWidget.FETCH_RESULT_ERROR_BATTERY
                    }
                else
                    updateAppWidgetState(context, glanceId) { preferences ->
                        preferences[LivePointWidget.FETCH_RESULT_KEY] =
                            LivePointWidget.FETCH_RESULT_ERROR_UNKNOWN
                    }
            }
            updater.update(context, glanceId)

            return false
        }

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.FETCH_RESULT_KEY] =
                LivePointWidget.FETCH_RESULT_RAN_COMPLETED
        }

        updater.update(context, glanceId)

        UpdateScheduler.schedule(context, appWidgetId, remainingTimes - 1, 30.seconds)
    }

    return true
}