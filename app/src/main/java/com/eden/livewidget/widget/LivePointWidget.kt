package com.eden.livewidget.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.eden.livewidget.Agency
import com.eden.livewidget.agencyFromString
import com.eden.livewidget.data.arrivals.ArrivalsData
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.widget.ui.MockContent
import com.eden.livewidget.widget.ui.MyContent
import com.eden.livewidget.widget.ui.MyContentMode
import com.eden.livewidget.widget.ui.PlaceholderContent
import kotlin.time.Duration

class LivePointWidget : GlanceAppWidget() {

    companion object {

        val AGENCY_KEY = stringPreferencesKey("agency")
        val API_VALUE_KEY = stringPreferencesKey("apiValue")
        val DISPLAY_NAME_KEY = stringPreferencesKey("displayName")
        val FETCH_RESULT_KEY = stringPreferencesKey("inactiveText")

        const val FETCH_RESULT_ERROR_UNKNOWN = "error-auth"
        const val FETCH_RESULT_ERROR_BATTERY = "error-battery"
        const val FETCH_RESULT_RAN_SKIPPED =  "ran-skipped"
        const val FETCH_RESULT_RAN_COMPLETED = "ran-received"
    }


    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {
            GlanceTheme {
                val agencyString = currentState(AGENCY_KEY)
                if (agencyString == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val agency = try {
                    agencyFromString(agencyString) as Agency
                } catch (_: Exception) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val apiValue = currentState(API_VALUE_KEY)
                if (apiValue == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val displayName = currentState(DISPLAY_NAME_KEY)
                if (displayName == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val fetchResultOptions = currentState(FETCH_RESULT_KEY)

                val repository = remember { ArrivalsRepository.getInstance(agency.apiProvider, apiValue) }
                val arrivalsData by repository.arrivalsData.collectAsState()

                val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
                val flow = UpdateScheduler.getIsActiveFlow(widgetId)
                val isActive by flow.collectAsState(false)

                val mode = when(fetchResultOptions) {
                    FETCH_RESULT_RAN_SKIPPED, FETCH_RESULT_RAN_COMPLETED -> {
                        when(arrivalsData.validity) {
                            ArrivalsData.Validity.VALID,
                            ArrivalsData.Validity.INVALID_UNINITIALIZED -> if (isActive) MyContentMode.ACTIVE else MyContentMode.PAUSED_OK_READY
                            ArrivalsData.Validity.INVALID_UNRESOLVED -> MyContentMode.PAUSED_ERROR_UNRESOLVED
                            ArrivalsData.Validity.INVALID_UNREACHABLE -> MyContentMode.PAUSED_ERROR_UNREACHABLE
                            ArrivalsData.Validity.INVALID_AUTHENTICATION -> MyContentMode.PAUSED_ERROR_AUTHENTICATE
                        }
                    }
                    FETCH_RESULT_ERROR_BATTERY -> MyContentMode.PAUSED_ERROR_BATTERY
                    FETCH_RESULT_ERROR_UNKNOWN -> MyContentMode.PAUSED_ERROR_UNKNOWN
                    else -> MyContentMode.PAUSED_ERROR_UNKNOWN
                }


                MyContent(mode, displayName, agency, arrivalsData.lastValidData)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {

        provideContent {
            GlanceTheme {
                MockContent()
            }
        }
    }
}

class RefreshLivePointWidgetCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {


        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        UpdateScheduler.schedule(context, widgetId, 3, Duration.ZERO)
    }
}
