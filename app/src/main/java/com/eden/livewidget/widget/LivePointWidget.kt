package com.eden.livewidget.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.eden.livewidget.Agency
import com.eden.livewidget.agencyFromString
import com.eden.livewidget.data.common.arrivals.Data
import com.eden.livewidget.data.common.arrivals.Repository
import com.eden.livewidget.data.common.points.Value
import com.eden.livewidget.data.format
import com.eden.livewidget.data.common.arrivals.filter.emptyState as emptyFilterState
import com.eden.livewidget.widget.ui.MockContent
import com.eden.livewidget.widget.ui.MyContent
import com.eden.livewidget.widget.ui.MyContentMode
import com.eden.livewidget.widget.ui.PlaceholderContent
import com.eden.livewidget.widget.update.UpdateScheduler

class LivePointWidget : GlanceAppWidget() {

    companion object {

        val AGENCY_KEY = stringPreferencesKey("agency:0")
        val VALUES_KEY = stringPreferencesKey("values:0")
        val NAME_KEY = stringPreferencesKey("name:0")
        val FETCH_STATE_KEY = stringPreferencesKey("fetchState:0")
        val FILTER_STATE_KEY = stringPreferencesKey("filterState:1")

        const val FETCH_PENDING = "pending"
        const val FETCH_RESULT_ERROR_UNKNOWN = "result-error-unknown"
        const val FETCH_RESULT_ERROR_AUTHENTICATION = "result-error-authentication"
        const val FETCH_RESULT_ERROR_UNREACHABLE = "result-error-unreachable"
        const val FETCH_RESULT_ERROR_UNRESOLVED = "result-error-unresolvable"
        const val FETCH_RESULT_ERROR_BATTERY = "result-error-battery"
        const val FETCH_RESULT_ERROR_METERED = "result-error-metered"
        const val FETCH_RESULT_RAN_SKIPPED =  "result-ran-skipped"
        const val FETCH_RESULT_RAN_COMPLETED = "result-ran-received"
    }

    override val previewSizeMode = SizeMode.Responsive(setOf(DpSize(160.dp, 80.dp), DpSize(1000.dp, 1000.dp)))


    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {
            GlanceTheme {
                Log.i(javaClass.name, "Restart")

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

                val valuesString = currentState(VALUES_KEY)
                val values: List<Value> = try {
                    checkNotNull(valuesString)
                    format.decodeFromString(valuesString)
                } catch (_: Exception) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }

                val displayName = currentState(NAME_KEY)
                if (displayName == null) {
                    PlaceholderContent(context, id)
                    return@GlanceTheme
                }


                val filterStateString = currentState(FILTER_STATE_KEY)
                val filterState = try {
                    if (filterStateString == null)
                        emptyFilterState()
                    else
                        format.decodeFromString(filterStateString)
                } catch (_: Exception) {
                    emptyFilterState()
                }

                val fetchResultOptions = currentState(FETCH_STATE_KEY)

                val repository = Repository.getInstance(
                    key = Repository.Companion.Key(
                        agencyString = agencyString,
                        valuesString = valuesString,
                        filterStateString = filterStateString ?: "",
                    ),
                    provider = agency.apiProvider,
                    values = values,
                    filterState = filterState
                )
                val arrivalsData by repository.arrivalsData.collectAsState()

                val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
                val flow = UpdateScheduler.getIsActiveFlow(widgetId)
                val isActive by flow.collectAsState(false)

                Log.i(this.javaClass.name, "Status: ${arrivalsData.validity} $fetchResultOptions $isActive")

                val mode =
                    if (isActive)
                        when(fetchResultOptions) {
                            FETCH_PENDING -> MyContentMode.ACTIVE_UNINITIALIZED
                            FETCH_RESULT_RAN_SKIPPED,
                            FETCH_RESULT_RAN_COMPLETED ->
                                when(arrivalsData.validity) {
                                    Data.Validity.VALID -> MyContentMode.ACTIVE_VALID
                                    Data.Validity.INVALID -> MyContentMode.ACTIVE_UNINITIALIZED
                                }
                            else -> MyContentMode.ACTIVE_RETRY
                        }
                    else
                        when(fetchResultOptions) {
                            FETCH_RESULT_RAN_SKIPPED,
                            FETCH_RESULT_RAN_COMPLETED ->
                                when(arrivalsData.validity) {
                                    Data.Validity.VALID -> MyContentMode.PAUSED_OK_READY
                                    Data.Validity.INVALID -> MyContentMode.PAUSED_ERROR_UNKNOWN
                                }
                            FETCH_RESULT_ERROR_UNRESOLVED -> MyContentMode.PAUSED_ERROR_UNRESOLVED
                            FETCH_RESULT_ERROR_UNREACHABLE -> MyContentMode.PAUSED_ERROR_UNREACHABLE
                            FETCH_RESULT_ERROR_AUTHENTICATION -> MyContentMode.PAUSED_ERROR_AUTHENTICATE
                            FETCH_RESULT_ERROR_METERED -> MyContentMode.PAUSED_ERROR_METERED
                            FETCH_RESULT_ERROR_BATTERY -> MyContentMode.PAUSED_ERROR_BATTERY
                            FETCH_RESULT_ERROR_UNKNOWN -> MyContentMode.PAUSED_ERROR_UNKNOWN
                            else -> MyContentMode.PAUSED_ERROR_UNKNOWN
                        }

                val updateTime = if (fetchResultOptions != FETCH_PENDING) arrivalsData.lastUpdate else null

                MyContent(mode, widgetId, displayName, agency, updateTime, arrivalsData.lastValidData)
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

