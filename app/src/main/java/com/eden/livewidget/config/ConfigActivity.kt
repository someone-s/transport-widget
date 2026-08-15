package com.eden.livewidget.config

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.eden.livewidget.Agency
import com.eden.livewidget.agencyFromString
import com.eden.livewidget.agencyToString
import com.eden.livewidget.config.ui.ConfigScreen
import com.eden.livewidget.configurator.ui.ConfiguratorContent
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.providerToString
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.widget.LivePointWidget
import com.eden.livewidget.widget.update.UpdateScheduler
import kotlinx.coroutines.launch

class ConfigActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, resultValue)

        val manager = GlanceAppWidgetManager(this)
        val glanceId = try {
            manager.getGlanceIdBy(appWidgetId)
        } catch (_: IllegalArgumentException) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            TransportWidgetsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    val (currentAgency, setCurrentAgency) = remember { mutableStateOf<Agency?>(null) }
                    val (currentPoint, setCurrentPoint) = remember { mutableStateOf<PointInfo?>(null) }
                    val (anyChanges, setAnyChanges) = remember { mutableStateOf(false) }

                    val coroutineScope = rememberCoroutineScope()

                    ConfigScreen(
                        currentAgency = currentAgency,
                        setCurrentAgency = { newAgency ->
                            if (newAgency != currentAgency)
                                setCurrentPoint(null)
                            setCurrentAgency(newAgency)
                            setAnyChanges(true)
                        },
                        currentPoint = currentPoint,
                        setCurrentPoint = { newPoint ->
                            setCurrentPoint(newPoint)
                            setAnyChanges(true)
                        },
                        anyChanges = anyChanges,
                        onSaveChanges = {
                            if (currentAgency != null && currentPoint != null)
                                coroutineScope.launch {
                                    createWidget(
                                        context = this@ConfigActivity,
                                        appWidgetId = appWidgetId,
                                        glanceId = glanceId,
                                        agency = currentAgency,
                                        point = currentPoint,
                                    )
                                }
                            else
                                finish()
                        },
                        onDiscardChanges = {
                            finish()
                        },
                    )

                    LaunchedEffect(Unit) {
                        // PreferencesGlanceStateDefinition is the default state definition used
                        val preferences = getAppWidgetState(this@ConfigActivity, PreferencesGlanceStateDefinition, glanceId)

                        val fetchedAgency = agencyFromString(preferences[LivePointWidget.AGENCY_KEY])
                        if (currentAgency == null)
                            setCurrentAgency(fetchedAgency)

                        val fetchedDisplayName = preferences[LivePointWidget.DISPLAY_NAME_KEY]
                        val fetchedApiValue = preferences[LivePointWidget.API_VALUE_KEY]
                        if (currentPoint == null)
                            setCurrentPoint(
                                if (fetchedDisplayName != null && fetchedApiValue != null)
                                    PointInfo(
                                        name = fetchedDisplayName,
                                        apiValue = fetchedApiValue
                                    )
                                else
                                    null
                            )
                    }
                }

            }
        }
    }


    private suspend fun createWidget(
        context: Context,
        appWidgetId: Int,
        glanceId: GlanceId,
        agency: Agency,
        point: PointInfo,
    ) {


        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)

        // Stop any ongoing update
        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.AGENCY_KEY] = agencyToString(agency)
            preferences[LivePointWidget.DISPLAY_NAME_KEY] = point.name
            preferences[LivePointWidget.API_VALUE_KEY] = point.apiValue
            preferences[LivePointWidget.FETCH_STATE_KEY] = LivePointWidget.FETCH_RESULT_RAN_SKIPPED
        }

        LivePointWidget().update(context, glanceId)

        finish()
    }
}