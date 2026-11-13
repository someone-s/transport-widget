package com.eden.livewidget.logic.update

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.PowerManager
import android.util.Log
import androidx.glance.appwidget.AppWidgetId
import androidx.glance.appwidget.AppWidgetSession
import com.eden.livewidget.data.AppContainer
import com.eden.livewidget.data.agency.common.FetchResult
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.common.point.FloatLogicalPoint
import com.eden.livewidget.data.location.repository.LocationRepository
import com.eden.livewidget.logic.emptyFilterState
import com.eden.livewidget.data.widget.repository.WidgetRepository
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.ui.widget.LivePointWidget
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toKotlinDuration

class UpdateLogic(
    private val context: Context,
    private val appContainer: AppContainer,
    private val widgetRepository: WidgetRepository,
    private val locationRepository: LocationRepository,
) {
    private class ExitEarlyException: RuntimeException()

    /**
     * Call [end] afterward to update widget one more time with reset state
     */
    suspend fun update(appWidgetId: Int) {

        widgetRepository.setIsActive(appWidgetId, true)

        val config = widgetRepository.getConfig(appWidgetId)
        try {
            if (config == null) throw ExitEarlyException()

            var remainingDuration = config.fetchDuration

            while (remainingDuration.isPositive) {
                beginWidgetState(appWidgetId)
                refreshWidgetUi(appWidgetId)
                val stateSuccess = refreshWidgetState(appWidgetId)
                refreshWidgetUi(appWidgetId)
                if (!stateSuccess) throw ExitEarlyException()

                delay(config.fetchInterval.toKotlinDuration())

                remainingDuration -= config.fetchInterval
            }
        }
        catch (_: ExitEarlyException) {
            Log.i("$javaClass", "$appWidgetId update exited early.")
        }
    }

    suspend fun end(appWidgetId: Int) {
        widgetRepository.setIsActive(appWidgetId, false)
        refreshWidgetUi(appWidgetId)
    }

    suspend fun delete(appWidgetId: Int) {
        widgetRepository.deleteConfig(appWidgetId)
    }

    suspend fun isAnyActive() = widgetRepository.isAnyActive()

    @Throws(ExitEarlyException::class)
    private suspend fun refreshWidgetUi(appWidgetId: Int) {

        // Glance use this instance to generate RemoteView to show
        val updater = LivePointWidget()

        val sessionId = "appWidget-$appWidgetId"

        Log.i("$javaClass", "Beginning custom glance session.")
        @SuppressLint("RestrictedApi")
        updater.getSessionManager(context).runWithLock {
            val wasRunning = isSessionRunning(context, sessionId)
            if (!wasRunning)
                startSession(context, AppWidgetSession(updater, AppWidgetId(appWidgetId)))
            val session = getSession(sessionId) as AppWidgetSession
            session.updateGlance()
        }

        Log.i("$javaClass", "Allow custom glance session to update for some duration.")
        delay(500.milliseconds)

        Log.i("$javaClass", "Ending custom glance session.")
        @SuppressLint("RestrictedApi")
        updater.getSessionManager(context).runWithLock {
            closeSession(sessionId)
        }
        Log.i("$javaClass", "Ended custom glance session.")
    }

    private suspend fun beginWidgetState(appWidgetId: Int) {
        widgetRepository.setFetchState(appWidgetId, FetchState.PENDING)
    }

    private suspend fun refreshWidgetState(appWidgetId: Int): Boolean {

        val fetchState = refreshArrivalsState(appWidgetId).let {
            when (it) {
                FetchState.RESULT_ERROR_UNKNOWN,
                FetchState.RESULT_ERROR_NETWORK_UNREACHABLE,
                FetchState.RESULT_ERROR_NETWORK_UNRESOLVED -> identityIssue() ?: it
                else -> it
            }
        }

        widgetRepository.setFetchState(appWidgetId, fetchState)

        return when (fetchState) {
            FetchState.RESULT_RAN_COMPLETED -> true
            else -> false
        }
    }

    private suspend fun refreshArrivalsState(appWidgetId: Int): FetchState {

        val widgetConfig = widgetRepository.getConfig(appWidgetId)
            ?: return FetchState.RESULT_ERROR_UNKNOWN

        val agency = widgetConfig.agencyId?.let { appContainer.agencyFromId(it) }
            ?: return FetchState.RESULT_ERROR_UNKNOWN
        val provider = agency.provider

        val logicalPoint = widgetConfig.point
            ?: return FetchState.RESULT_ERROR_UNKNOWN

        val dataPoints = when(logicalPoint) {
            is FixedLogicalPoint -> logicalPoint.dataPoints
            is FloatLogicalPoint -> {
                val location = try {
                    locationRepository.getLocation(LocationRepository.Accuracy.FINE)
                } catch (_: LocationRepository.MissingPermissionException) {
                    return FetchState.RESULT_ERROR_LOCATION_PERMISSION
                } catch (_: LocationRepository.UnavailableException) {
                    return FetchState.RESULT_ERROR_LOCATION_DISABLED
                }

                val locationAwarePointRepository = provider.pointsRepository.asLocationAware ?:
                    return FetchState.RESULT_ERROR_UNKNOWN

                val (locationResult, actualPoint) =
                    locationAwarePointRepository.getClosest(location.latitude, location.longitude)

                when (locationResult) {
                    FetchResult.SUCCESS -> actualPoint?.dataPoints ?: return FetchState.RESULT_ERROR_UNKNOWN
                    FetchResult.ERROR_NO_DATA,
                    FetchResult.ERROR_SCHEMA -> return FetchState.RESULT_ERROR_UNKNOWN
                    FetchResult.ERROR_AUTHENTICATION -> return FetchState.RESULT_ERROR_NETWORK_AUTHENTICATION
                    FetchResult.ERROR_UNREACHABLE -> return FetchState.RESULT_ERROR_NETWORK_UNREACHABLE
                    FetchResult.ERROR_UNRESOLVED -> return FetchState.RESULT_ERROR_NETWORK_UNRESOLVED
                }
            }
            else -> {
                Log.e("$javaClass", "Unexpected logical point type")
                return FetchState.RESULT_ERROR_UNKNOWN
            }
        }

        val filters = widgetConfig.filters ?: emptyFilterState()

        try {
            // Update data source
            val result = provider.arrivalsRepository.updateState(
                key = appWidgetId,
                dataPoints = dataPoints,
                destinationFilters = filters.destinations.mapNotNull { it.filters }.flatten()
            )

            return when (result) {
                FetchResult.SUCCESS -> FetchState.RESULT_RAN_COMPLETED
                FetchResult.ERROR_NO_DATA,
                FetchResult.ERROR_SCHEMA -> FetchState.RESULT_ERROR_UNKNOWN
                FetchResult.ERROR_AUTHENTICATION -> FetchState.RESULT_ERROR_NETWORK_AUTHENTICATION
                FetchResult.ERROR_UNREACHABLE -> FetchState.RESULT_ERROR_NETWORK_UNREACHABLE
                FetchResult.ERROR_UNRESOLVED -> FetchState.RESULT_ERROR_NETWORK_UNRESOLVED
            }

        } catch (e: Exception) {
            Log.e("${e.javaClass}", e.message ?: "Failed with no message", e)
            return FetchState.RESULT_ERROR_UNKNOWN
        }
    }

    private fun identityIssue(): FetchState? {

        fun tryIdentifyMeteredNetworkIssue(): FetchState? {

            val connectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
                ?: return null

            connectivityManager.apply {
                return if (isActiveNetworkMetered && restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED)
                    FetchState.RESULT_ERROR_NETWORK_METERED
                else
                    null
            }
        }

        fun tryIdentifyBatteryOptimizationIssue(): FetchState? {

            val powerManager = (context.getSystemService(Context.POWER_SERVICE) as? PowerManager)
                ?: return null

            powerManager.apply {
                return if (isPowerSaveMode && isIgnoringBatteryOptimizations(context.packageName))
                    null
                else
                    FetchState.RESULT_ERROR_BATTERY
            }
        }

        with(tryIdentifyMeteredNetworkIssue()) {
            if (this@with != null) return this@with
        }
        with(tryIdentifyBatteryOptimizationIssue()) {
            if (this@with != null) return this@with
        }
        return null
    }

}

