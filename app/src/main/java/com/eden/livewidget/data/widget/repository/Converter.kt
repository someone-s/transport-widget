package com.eden.livewidget.data.widget.repository

import com.eden.livewidget.data.agency.formatter
import com.eden.livewidget.data.widget.database.ConfigEntity
import com.eden.livewidget.data.widget.database.StateEntity
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetConfig
import com.eden.livewidget.data.widget.structure.WidgetState
import java.time.Duration

fun WidgetConfig.toConfigEntity() = ConfigEntity(
    appWidgetId = this.appWidgetId,
    agencyId = this.agencyId,
    pointString = this.point?.let { formatter.encodeToString(it) },
    filtersString = this.filters?.let { formatter.encodeToString(it) },
    fetchDuration = this.fetchDuration.toNanos(),
    fetchInterval = this.fetchInterval.toNanos(),
)

fun ConfigEntity.toWidgetConfig() = WidgetConfig(
    appWidgetId = this.appWidgetId,
    agencyId = this.agencyId,
    point = this.pointString?.let { formatter.decodeFromString(it) },
    filters = this.filtersString?.let { formatter.decodeFromString(it) },
    fetchDuration = Duration.ofNanos(this.fetchDuration),
    fetchInterval = Duration.ofNanos(this.fetchInterval),
)

fun FetchState.toSerial() = when(this) {
    FetchState.PENDING -> StateEntity.PENDING
    FetchState.RESULT_ERROR_UNKNOWN -> StateEntity.RESULT_ERROR_UNKNOWN
    FetchState.RESULT_ERROR_NETWORK_AUTHENTICATION -> StateEntity.RESULT_ERROR_NETWORK_AUTHENTICATION
    FetchState.RESULT_ERROR_NETWORK_UNREACHABLE -> StateEntity.RESULT_ERROR_NETWORK_UNREACHABLE
    FetchState.RESULT_ERROR_NETWORK_UNRESOLVED -> StateEntity.RESULT_ERROR_NETWORK_UNRESOLVED
    FetchState.RESULT_ERROR_LOCATION_DISABLED -> StateEntity.RESULT_ERROR_LOCATION_DISABLED
    FetchState.RESULT_ERROR_LOCATION_PERMISSION -> StateEntity.RESULT_ERROR_LOCATION_PERMISSION
    FetchState.RESULT_ERROR_NETWORK_METERED -> StateEntity.RESULT_ERROR_NETWORK_METERED
    FetchState.RESULT_ERROR_BATTERY -> StateEntity.RESULT_ERROR_BATTERY
    FetchState.RESULT_RAN_COMPLETED -> StateEntity.RESULT_RAN_COMPLETED
}

fun String.toFetchState() = when(this) {
    StateEntity.PENDING -> FetchState.PENDING
    StateEntity.RESULT_ERROR_UNKNOWN -> FetchState.RESULT_ERROR_UNKNOWN
    StateEntity.RESULT_ERROR_NETWORK_AUTHENTICATION -> FetchState.RESULT_ERROR_NETWORK_AUTHENTICATION
    StateEntity.RESULT_ERROR_NETWORK_UNREACHABLE -> FetchState.RESULT_ERROR_NETWORK_UNREACHABLE
    StateEntity.RESULT_ERROR_NETWORK_UNRESOLVED -> FetchState.RESULT_ERROR_NETWORK_UNRESOLVED
    StateEntity.RESULT_ERROR_LOCATION_DISABLED -> FetchState.RESULT_ERROR_LOCATION_DISABLED
    StateEntity.RESULT_ERROR_LOCATION_PERMISSION -> FetchState.RESULT_ERROR_LOCATION_PERMISSION
    StateEntity.RESULT_ERROR_NETWORK_METERED -> FetchState.RESULT_ERROR_NETWORK_METERED
    StateEntity.RESULT_ERROR_BATTERY -> FetchState.RESULT_ERROR_BATTERY
    StateEntity.RESULT_RAN_COMPLETED -> FetchState.RESULT_RAN_COMPLETED
    else -> throw IllegalArgumentException("\"$this\" is not an expected value.")
}

fun WidgetState.toStateEntity(appWidgetId: Int) = StateEntity(
    appWidgetId = appWidgetId,
    isActive = this.isActive,
    fetchState = this.fetchState.toSerial(),
)

fun StateEntity.toWidgetState() = WidgetState(
    isActive = this.isActive,
    fetchState = this.fetchState.toFetchState(),
)