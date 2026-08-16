package com.eden.livewidget.config.ui.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.filter.destination.Filter
import com.eden.livewidget.data.common.points.Model
import kotlin.uuid.Uuid

@Composable
fun Group(
    currentProvider: Provider?,
    currentPoint: Model?,
    filters: Map<Uuid, Filter>?,
    addFilter: (Filter) -> Unit,
    updateFilter: (Uuid, Filter) -> Unit,
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

