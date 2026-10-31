package com.eden.livewidget.ui.config.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.arrivals.filter.destination.DestinationConfig
import com.eden.livewidget.data.common.LogicalPoint
import kotlin.uuid.Uuid

@Composable
fun Group(
    currentProvider: Provider?,
    currentPoint: LogicalPoint?,
    filters: Map<Uuid, DestinationConfig>?,
    addFilter: (DestinationConfig) -> Unit,
    updateFilter: (Uuid, DestinationConfig) -> Unit,
    removeFilter: (Uuid) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        filters?.onEachIndexed { index, (id, filter) ->
            Item(
                currentProvider = currentProvider,
                currentPoint = currentPoint,
                filter = filter,
                onFilterChange = { newFilter ->
                    if (newFilter == null)
                        removeFilter(id)
                    else
                        updateFilter(id, newFilter)
                },
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    filters.size + 1,
                ),
            )
        }

        AddButton(
            currentProvider = currentProvider,
            currentPoint = currentPoint,
            onAddFilter = addFilter,
            shapes = ListItemDefaults.segmentedShapes(
                filters?.size ?: 0,
                (filters?.size ?: 0) + 1
            ),
        )
    }
}

