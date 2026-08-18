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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.eden.livewidget.Agency
import com.eden.livewidget.agencyFromString
import com.eden.livewidget.agencyToString
import com.eden.livewidget.config.ui.ConfigScreen
import com.eden.livewidget.data.common.filter.destination.DestinationCompiler
import com.eden.livewidget.data.common.filter.State
import com.eden.livewidget.data.common.filter.destination.Filter
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.pointsFormat
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import com.eden.livewidget.widget.LivePointWidget
import com.eden.livewidget.widget.update.UpdateScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

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
                    val (currentPoint, setCurrentPoint) = remember { mutableStateOf<Model?>(null) }
                    val destinationFilters = remember { mutableStateMapOf<Uuid, Filter>() }
                    val destinationFilterCompiler = remember(currentAgency) { currentAgency?.apiProvider?.filterConstructors?.destinationCompilerConstructor() }
                    val (anyChanges, setAnyChanges) = remember { mutableStateOf(false) }

                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        // PreferencesGlanceStateDefinition is the default state definition used
                        val preferences = getAppWidgetState(this@ConfigActivity, PreferencesGlanceStateDefinition, glanceId)

                        val fetchedAgency = agencyFromString(preferences[LivePointWidget.AGENCY_KEY])
                        if (currentAgency == null)
                            setCurrentAgency(fetchedAgency)

                        val fetchedName = preferences[LivePointWidget.NAME_KEY]
                        val fetchedValues = preferences[LivePointWidget.VALUES_KEY]
                        if (currentPoint == null)
                            setCurrentPoint(
                                if (fetchedName != null && fetchedValues != null)
                                    Model(
                                        name = fetchedName,
                                        values = pointsFormat.decodeFromString(fetchedValues)
                                    )
                                else
                                    null
                            )

                        val filterStateString = preferences[LivePointWidget.FILTER_STATE_KEY]
                        if (filterStateString != null) {
                            val filterState = State.deserialize(filterStateString)
                            destinationFilters.putAll(
                                filterState.destinationFilters.map { Pair(Uuid.random(), it) }
                            )
                        }
                    }

                    ConfigScreen(
                        currentAgency = currentAgency,
                        setCurrentAgency = { newAgency ->
                            if (newAgency != currentAgency) {
                                setCurrentPoint(null)
                                destinationFilters.clear()
                            }
                            setCurrentAgency(newAgency)
                            setAnyChanges(true)
                        },
                        currentPoint = currentPoint,
                        setCurrentPoint = { newPoint ->
                            if (newPoint != currentPoint) {
                                destinationFilters.clear()
                            }
                            setCurrentPoint(newPoint)
                            setAnyChanges(true)
                        },
                        destinationFilters = destinationFilters,
                        addDestinationFilter = { newFilter ->
                            val id = Uuid.random()
                            destinationFilters[id] = newFilter
                            coroutineScope.launch {
                                if (destinationFilterCompiler == null) return@launch

                                val compiledFilter = withContext(Dispatchers.IO) {
                                    destinationFilterCompiler.compileFilter(newFilter)
                                }
                                destinationFilters.replace(id, compiledFilter)
                            }
                            setAnyChanges(true)
                        },
                        updateDestinationFilter = function(
                            destinationFilters,
                            coroutineScope,
                            destinationFilterCompiler,
                            setAnyChanges
                        ),
                        removeDestinationFilter = { id ->
                            destinationFilters.remove(id)
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
                                        filterState = State(
                                            destinationFilters = destinationFilters.values.toList(),
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

    @Composable
    private fun function(
        destinationFilters: SnapshotStateMap<Uuid, Filter>,
        coroutineScope: CoroutineScope,
        destinationFilterCompiler: DestinationCompiler?,
        setAnyChanges: (Boolean) -> Unit
    ): (Uuid, Filter) -> Unit {
        return { id, newFilter ->
            destinationFilters[id] = newFilter
            coroutineScope.launch {
                if (destinationFilterCompiler == null) return@launch

                val compiledFilter = withContext(Dispatchers.IO) {
                    destinationFilterCompiler.compileFilter(newFilter)
                }
                destinationFilters.replace(id, compiledFilter)
            }
            setAnyChanges(true)
        }
    }


    private suspend fun createWidget(
        context: Context,
        appWidgetId: Int,
        glanceId: GlanceId,
        agency: Agency,
        point: Model,
        filterState: State,
    ) {


        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)

        // Stop any ongoing update
        UpdateScheduler.cancelCurrentRequest(context, appWidgetId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.AGENCY_KEY] = agencyToString(agency)
            preferences[LivePointWidget.NAME_KEY] = point.name
            preferences[LivePointWidget.VALUES_KEY] = pointsFormat.encodeToString(point.values)
            preferences[LivePointWidget.FETCH_STATE_KEY] = LivePointWidget.FETCH_RESULT_RAN_SKIPPED
            preferences[LivePointWidget.FILTER_STATE_KEY] = State.serialize(filterState)
        }

        LivePointWidget().update(context, glanceId)

        finish()
    }
}