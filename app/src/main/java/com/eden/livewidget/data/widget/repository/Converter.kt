package com.eden.livewidget.data.widget.repository

import com.eden.livewidget.data.agency.formatter
import com.eden.livewidget.data.widget.database.ConfigEntity
import com.eden.livewidget.data.widget.database.StateEntity
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState

fun WidgetConfig.toConfigEntity() = ConfigEntity(
    appWidgetId = this.appWidgetId,
    agencyId = this.agencyId,
    pointString = this.point?.let { formatter.encodeToString(it) },
    filtersString = this.filters?.let { formatter.encodeToString(it) },
)

fun ConfigEntity.toWidgetConfig() = WidgetConfig(
    appWidgetId = this.appWidgetId,
    agencyId = this.agencyId,
    point = this.pointString?.let { formatter.decodeFromString(it) },
    filters = this.filtersString?.let { formatter.decodeFromString(it) },
)

fun WidgetState.toStateEntity(appWidgetId: Int) = StateEntity(
    appWidgetId = appWidgetId,
    fetchState = when(this.fetchState) {
        FetchState.PENDING -> StateEntity.PENDING
        FetchState.RESULT_ERROR_UNKNOWN -> StateEntity.RESULT_ERROR_UNKNOWN
        FetchState.RESULT_ERROR_AUTHENTICATION -> StateEntity.RESULT_ERROR_AUTHENTICATION
        FetchState.RESULT_ERROR_UNREACHABLE -> StateEntity.RESULT_ERROR_UNREACHABLE
        FetchState.RESULT_ERROR_UNRESOLVED -> StateEntity.RESULT_ERROR_UNRESOLVED
        FetchState.RESULT_ERROR_METERED -> StateEntity.RESULT_ERROR_METERED
        FetchState.RESULT_ERROR_BATTERY -> StateEntity.RESULT_ERROR_BATTERY
        FetchState.RESULT_RAN_SKIPPED -> StateEntity.RESULT_RAN_SKIPPED
        FetchState.RESULT_RAN_COMPLETED -> StateEntity.RESULT_RAN_COMPLETED
    }
)

fun StateEntity.toWidgetState() = WidgetState(
    fetchState = when(this.fetchState) {
        StateEntity.PENDING -> FetchState.PENDING
        StateEntity.RESULT_ERROR_UNKNOWN -> FetchState.RESULT_ERROR_UNKNOWN
        StateEntity.RESULT_ERROR_AUTHENTICATION -> FetchState.RESULT_ERROR_AUTHENTICATION
        StateEntity.RESULT_ERROR_UNREACHABLE -> FetchState.RESULT_ERROR_UNREACHABLE
        StateEntity.RESULT_ERROR_UNRESOLVED -> FetchState.RESULT_ERROR_UNRESOLVED
        StateEntity.RESULT_ERROR_METERED -> FetchState.RESULT_ERROR_METERED
        StateEntity.RESULT_ERROR_BATTERY -> FetchState.RESULT_ERROR_BATTERY
        StateEntity.RESULT_RAN_SKIPPED -> FetchState.RESULT_RAN_SKIPPED
        StateEntity.RESULT_RAN_COMPLETED -> FetchState.RESULT_RAN_COMPLETED
        else -> throw IllegalArgumentException("\"${this.fetchState}\" is not an expected value.")
    }
)