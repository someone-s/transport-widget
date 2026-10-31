package com.eden.livewidget.ui.config.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.data.common.DestinationConfig
import com.eden.livewidget.data.common.LogicalPoint
import com.eden.livewidget.data.common.Provider
import kotlin.uuid.Uuid

@Composable
fun DestinationsGroup(
    agencyId: String,
    viewModel: DestinationsViewModel by viewModels { DestinationsViewModelFactory(agencyId) }
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

        AddSection(
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

