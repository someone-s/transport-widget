package com.eden.livewidget.ui.config

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.eden.livewidget.LiveWidgetApplication
import com.eden.livewidget.data.Agency
import com.eden.livewidget.logic.FiltersState
import com.eden.livewidget.data.common.LogicalPoint
import com.eden.livewidget.data.formatter
import com.eden.livewidget.logic.DestinationState
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.ui.widget.LivePointWidget
import com.eden.livewidget.logic.update.UpdateScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class ConfigActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as LiveWidgetApplication).container

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

                    var currentAgency by remember { mutableStateOf<Agency?>(null) }
                    var currentPoint by remember { mutableStateOf<LogicalPoint?>(null) }
                    val destinations = remember { mutableStateMapOf<Uuid, DestinationState>() }
                    val destinationCompiler = remember(currentAgency) { currentAgency?.provider?.destinationCompiler }
                    var anyChanges by remember { mutableStateOf(false) }

                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        // PreferencesGlanceStateDefinition is the default state definition used
                        val preferences = getAppWidgetState(this@ConfigActivity, PreferencesGlanceStateDefinition, glanceId)

                        val fetchedAgency = preferences[LivePointWidget.AGENCY_KEY]?.let { appContainer.agencyFromId(it) }
                        if (currentAgency == null)
                            currentAgency = fetchedAgency

                        val fetchedName = preferences[LivePointWidget.NAME_KEY]
                        val fetchedValues = preferences[LivePointWidget.VALUES_KEY]
                        if (currentPoint == null)
                            currentPoint = if (fetchedName != null && fetchedValues != null)
                                    LogicalPoint(
                                        name = fetchedName,
                                        dataPoints = formatter.decodeFromString(fetchedValues)
                                    )
                                else null

                        val filterStateString = preferences[LivePointWidget.FILTER_STATE_KEY]
                        if (filterStateString != null) {
                            val filterState: FiltersState = formatter.decodeFromString(filterStateString)
                            destinations.putAll(filterState.destinations.map { Pair(Uuid.random(), it) })
                        }
                    }

                    ConfigScreen(
                        agencies = appContainer.agencies,
                        currentAgency = currentAgency,
                        setCurrentAgency = { newAgency ->
                            if (newAgency != currentAgency) {
                                currentPoint = null
                                destinations.clear()
                            }
                            currentAgency = newAgency
                            anyChanges = true
                        },
                        currentPoint = currentPoint,
                        setCurrentPoint = { newPoint ->
                            if (newPoint != currentPoint) {
                                destinations.clear()
                            }
                            currentPoint = newPoint
                            anyChanges = true
                        },
                        destinationFilters = destinations,
                        addDestinationFilter = { newFilter ->
                            val id = Uuid.random()
                            destinations[id] = DestinationState(
                                config = newFilter
                            )
                            coroutineScope.launch {
                                if (destinationCompiler == null) return@launch

                                val compiledFilters = withContext(Dispatchers.IO) {
                                    destinationCompiler.compileFilter(newFilter)
                                }
                                destinations.replace(id, DestinationState(
                                    config = newFilter,
                                    filters = compiledFilters,
                                ))
                            }
                            anyChanges = true
                        },
                        updateDestinationFilter = { id, newFilter ->
                            destinations[id] = DestinationState(
                                config = newFilter
                            )
                            coroutineScope.launch {
                                if (destinationCompiler == null) return@launch

                                val compiledFilters = withContext(Dispatchers.IO) {
                                    destinationCompiler.compileFilter(newFilter)
                                }
                                destinations.replace(id, DestinationState(
                                    config = newFilter,
                                    filters = compiledFilters,
                                ))
                            }
                            anyChanges = true
                        },
                        removeDestinationFilter = { id ->
                            destinations.remove(id)
                            anyChanges = true
                        },
                        anyChanges = anyChanges,
                        onSaveChanges = {
                            val capturedAgency = currentAgency
                            val capturedPoint = currentPoint
                            if (capturedAgency != null && capturedPoint != null)
                                coroutineScope.launch {
                                    createWidget(
                                        context = this@ConfigActivity,
                                        appWidgetId = appWidgetId,
                                        glanceId = glanceId,
                                        agency = capturedAgency,
                                        point = capturedPoint,
                                        filterState = FiltersState(
                                            destinations = destinations.values.toList(),
                                        )
                                    )
                                }
                            else
                                finish()
                        },
                        onDiscardChanges = {
                            finish()
                        },
                    )


                }

            }
        }
    }

    private suspend fun createWidget(
        context: Context,
        appWidgetId: Int,
        glanceId: GlanceId,
        agency: Agency,
        point: LogicalPoint,
        filterState: FiltersState,
    ) {


        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)

        // Stop any ongoing update
        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.AGENCY_KEY] = agency.id
            preferences[LivePointWidget.NAME_KEY] = point.name
            preferences[LivePointWidget.VALUES_KEY] = formatter.encodeToString(point.dataPoints)
            preferences[LivePointWidget.FETCH_STATE_KEY] = LivePointWidget.FETCH_RESULT_RAN_SKIPPED
            preferences[LivePointWidget.FILTER_STATE_KEY] = formatter.encodeToString(filterState)
        }

        LivePointWidget().update(context, glanceId)

        finish()
    }
}