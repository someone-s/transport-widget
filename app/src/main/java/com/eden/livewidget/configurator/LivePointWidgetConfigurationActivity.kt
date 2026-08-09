package com.eden.livewidget.configurator

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.providerToString
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.configurator.ui.ConfiguratorContent
import com.eden.livewidget.widget.LivePointWidget
import com.eden.livewidget.widget.update.UpdateScheduler
import kotlinx.coroutines.launch

class LivePointWidgetConfigurationActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)


        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfiguratorContent { apiProvider, apiValue, displayName ->
                        val context = this
                        lifecycleScope.launch {
                            createWidget(
                                context,
                                appWidgetId,
                                apiProvider,
                                apiValue,
                                displayName
                            )
                        }
                    }

                }

            }
        }
    }


    private suspend fun createWidget(
        context: Context,
        appWidgetId: Int,
        apiProvider: Provider,
        apiValue: String,
        displayName: String
    ) {


        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)

        val manager = GlanceAppWidgetManager(context)
        var glanceId: GlanceId
        try {
            glanceId = manager.getGlanceIdBy(appWidgetId)
        } catch (_: IllegalArgumentException) {
            return
        }

        // Stop any ongoing update
        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.AGENCY_KEY] = providerToString(apiProvider)
            preferences[LivePointWidget.API_VALUE_KEY] = apiValue
            preferences[LivePointWidget.DISPLAY_NAME_KEY] = displayName
            preferences[LivePointWidget.FETCH_RESULT_KEY] = LivePointWidget.FETCH_RESULT_RAN_COMPLETED
        }

        LivePointWidget().update(context, glanceId)

        finish()
    }
}