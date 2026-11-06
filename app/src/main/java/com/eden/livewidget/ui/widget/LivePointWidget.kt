package com.eden.livewidget.ui.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.eden.livewidget.LiveWidgetApplication
import com.eden.livewidget.data.agency.common.arrivals.ArrivalsState
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.logic.emptyFilterState

class LivePointWidget : GlanceAppWidget() {


    override val previewSizeMode = SizeMode.Responsive(setOf(DpSize(160.dp, 80.dp), DpSize(1000.dp, 1000.dp)))


    override suspend fun provideGlance(context: Context, id: GlanceId) {


        val appContainer = (context.applicationContext as LiveWidgetApplication).container
        val appWidgetManager = GlanceAppWidgetManager(context)
        val appWidgetId = appWidgetManager.getAppWidgetId(id)
        val widgetRepository = appContainer.widgetRepository

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {

            val widgetConfig by widgetRepository.getConfigFlow(appWidgetId).collectAsState(null)
            val agency = widgetConfig?.agencyId?.let { appContainer.agencyFromId(it) }
            val point = widgetConfig?.point
            val filters = widgetConfig?.filters ?: emptyFilterState()

            val widgetState by widgetRepository.getStateFlow(appWidgetId).collectAsState(null)
            val fetchState = widgetState?.fetchState ?: FetchState.PENDING
            val isActive = widgetState?.isActive ?: false

            GlanceTheme {
                if (agency == null || point == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val arrivalsData by agency.provider.arrivalsRepository.getOrCreateFlow(appWidgetId).collectAsState()

                val mode =
                    if (isActive)
                        when(fetchState) {
                            FetchState.PENDING -> MyContentMode.ACTIVE_UNINITIALIZED
                            FetchState.RESULT_RAN_COMPLETED ->
                                when(arrivalsData.validity) {
                                    ArrivalsState.Validity.VALID -> MyContentMode.ACTIVE_VALID
                                    ArrivalsState.Validity.INVALID,
                                    ArrivalsState.Validity.UNINITIALIZED -> MyContentMode.ACTIVE_UNINITIALIZED
                                }
                            else -> MyContentMode.ACTIVE_RETRY
                        }
                    else
                        when(fetchState) {
                            FetchState.RESULT_RAN_COMPLETED ->
                                when(arrivalsData.validity) {
                                    ArrivalsState.Validity.UNINITIALIZED,
                                    ArrivalsState.Validity.VALID -> MyContentMode.PAUSED_OK_READY
                                    ArrivalsState.Validity.INVALID -> MyContentMode.PAUSED_ERROR_UNKNOWN
                                }
                            FetchState.RESULT_ERROR_UNRESOLVED -> MyContentMode.PAUSED_ERROR_UNRESOLVED
                            FetchState.RESULT_ERROR_UNREACHABLE -> MyContentMode.PAUSED_ERROR_UNREACHABLE
                            FetchState.RESULT_ERROR_AUTHENTICATION -> MyContentMode.PAUSED_ERROR_AUTHENTICATE
                            FetchState.RESULT_ERROR_METERED -> MyContentMode.PAUSED_ERROR_METERED
                            FetchState.RESULT_ERROR_BATTERY -> MyContentMode.PAUSED_ERROR_BATTERY
                            FetchState.RESULT_ERROR_UNKNOWN -> MyContentMode.PAUSED_ERROR_UNKNOWN
                            else -> MyContentMode.PAUSED_ERROR_UNKNOWN
                        }

                val updateTime = if (fetchState != FetchState.PENDING) arrivalsData.lastUpdate else null

                val destinationNames = filters.destinations.map { it.config.toPoint.name }

                MyContent(
                    mode = mode,
                    appWidgetId = appWidgetId,
                    fromName = point.name,
                    toNames = destinationNames,
                    agency = agency,
                    lastUpdate = updateTime,
                    lastValidData =  arrivalsData.lastValidData,
                )
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

