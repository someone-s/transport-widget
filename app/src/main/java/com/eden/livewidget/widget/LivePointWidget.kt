package com.eden.livewidget.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.eden.livewidget.R
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.arrivals.ArrivalsRepository
import com.eden.livewidget.data.providerFromString
import com.eden.livewidget.widget.ui.MockContent
import com.eden.livewidget.widget.ui.MyContent
import com.eden.livewidget.widget.ui.PlaceholderContent

class LivePointWidget : GlanceAppWidget() {

    companion object {

        val API_PROVIDER_KEY = stringPreferencesKey("apiProvider")
        val API_VALUE_KEY = stringPreferencesKey("apiValue")
        val DISPLAY_NAME_KEY = stringPreferencesKey("displayName")
        val INACTIVE_TEXT_OPTION_KEY = stringPreferencesKey("inactiveText")

        const val INACTIVE_TEXT_OPTION_ERROR = "error"
        const val INACTIVE_TEXT_OPTION_BATTERY = "battery"
        const val INACTIVE_TEXT_OPTION_NORMAL =  "normal"


    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {
            GlanceTheme {
                val apiProviderString = currentState(API_PROVIDER_KEY)
                if (apiProviderString == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                var apiProvider: Provider
                try {
                    apiProvider = providerFromString(apiProviderString) as Provider
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

                val inactiveTextOption = currentState(INACTIVE_TEXT_OPTION_KEY)
                val inactiveText = when (inactiveTextOption) {
                    INACTIVE_TEXT_OPTION_ERROR -> LocalContext.current.getString(R.string.widget_start_tracking_error_text)
                    INACTIVE_TEXT_OPTION_BATTERY -> LocalContext.current.getString(R.string.widget_start_tracking_battery_text)
                    else -> LocalContext.current.getString(R.string.widget_start_tracking_prompt_text)
                }

                val repository = remember { ArrivalsRepository.getInstance(apiProvider, apiValue) }
                val latestArrivals by repository.latestArrivals.collectAsState()
                MyContent(context, id, displayName, inactiveText, latestArrivals)
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
        LivePointWidgetUpdateWorker.schedule(context, widgetId, 3, null)
    }
}
