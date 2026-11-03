package com.eden.livewidget.logic.update

import android.content.Context
import android.net.ConnectivityManager
import android.os.PowerManager
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.eden.livewidget.data.AppContainer
import com.eden.livewidget.data.agency.common.arrivals.FetchResult
import com.eden.livewidget.logic.emptyFilterState
import com.eden.livewidget.data.widget.repository.WidgetRepository
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetState
import com.eden.livewidget.ui.widget.LivePointWidget

class UpdateLogic(
    private val widgetRepository: WidgetRepository,
) {

    suspend fun updateWidget(
        context: Context,
        appContainer: AppContainer,
        appWidgetId: Int,
        remainingTimes: Int
    ): Boolean {

        val manager = GlanceAppWidgetManager(context)
        // if illegal exception let worker fail
        val glanceId = manager.getGlanceIdBy(appWidgetId)

        // Glance use this instance to generate RemoteView to show
        val updater = LivePointWidget()

        val fetchState =
            if (remainingTimes < 0)
                FetchState.RESULT_RAN_SKIPPED
            else
                updateData(
                    appContainer = appContainer,
                    appWidgetId = appWidgetId
                ).let {
                    when (it) {
                        FetchState.RESULT_ERROR_UNKNOWN,
                        FetchState.RESULT_ERROR_UNREACHABLE,
                        FetchState.RESULT_ERROR_UNRESOLVED -> identityIssue(
                            context = context,
                            currentState = it
                        )
                        else -> it
                    }
                }

        widgetRepository.setState(
            appWidgetId = appWidgetId,
            state = WidgetState(fetchState)
        )

        updater.update(context, glanceId)

        return when (fetchState) {
            FetchState.RESULT_RAN_COMPLETED,
            FetchState.RESULT_RAN_SKIPPED -> true
            else -> false
        }
    }

    private suspend fun updateData(
        appContainer: AppContainer,
        appWidgetId: Int,
    ): FetchState {

        val widgetConfig = appContainer.widgetRepository.getConfig(appWidgetId)
            ?: return FetchState.RESULT_ERROR_UNKNOWN

        val agency = widgetConfig.agencyId?.let { appContainer.agencyFromId(it) }
            ?: return FetchState.RESULT_ERROR_UNKNOWN
        val provider = agency.provider

        val point = widgetConfig.point
            ?: return FetchState.RESULT_ERROR_UNKNOWN

        val filters = widgetConfig.filters ?: emptyFilterState()

        try {
            // Update data source
            val result = provider.arrivalsRepository.updateState(
                key = appWidgetId,
                dataPoints = point.dataPoints,
                destinationFilters = filters.destinations.mapNotNull { it.filters }.flatten()
            )

            return when (result) {
                FetchResult.SUCCESS -> FetchState.RESULT_RAN_COMPLETED
                FetchResult.ERROR_AUTHENTICATION -> FetchState.RESULT_ERROR_AUTHENTICATION
                FetchResult.ERROR_UNREACHABLE -> FetchState.RESULT_ERROR_UNREACHABLE
                FetchResult.ERROR_UNRESOLVED -> FetchState.RESULT_ERROR_UNRESOLVED
            }

        } catch (e: Exception) {
            Log.e("${e.javaClass}", e.message ?: "Failed with no message", e)
            return FetchState.RESULT_ERROR_UNKNOWN
        }
    }

    private fun identityIssue(
        context: Context,
        currentState: FetchState,
    ): FetchState =
        UpdateResultBuilder(
            context = context,
            storedState = currentState,
        )
            .tryIdentifyMeteredNetworkIssue()
            .tryIdentifyBatteryOptimizationIssue()
            .build()

    private data class UpdateResultBuilder(
        private val context: Context,
        private val storedState: FetchState = FetchState.RESULT_ERROR_UNKNOWN,
    ) {
        fun build(): FetchState = storedState

        fun tryIdentifyMeteredNetworkIssue(): UpdateResultBuilder {

            val currentBuilder = this

            val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                ?: return currentBuilder

            connectivityManager.apply {
                return if (isActiveNetworkMetered && restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED)
                    UpdateResultBuilder(context, FetchState.RESULT_ERROR_METERED)
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
                    UpdateResultBuilder(context, FetchState.RESULT_ERROR_BATTERY)
            }
        }
    }
}

