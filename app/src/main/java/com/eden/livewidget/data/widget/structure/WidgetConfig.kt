package com.eden.livewidget.data.widget.structure

import com.eden.livewidget.data.agency.common.point.LogicalPoint
import com.eden.livewidget.logic.FiltersState

data class WidgetConfig(
    val appWidgetId: Int,
    val agencyId: String?,
    val point: LogicalPoint?,
    val filters: FiltersState?,
)