package com.eden.livewidget.ui.config.destination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.point.LogicalPoint
import com.eden.livewidget.data.agency.common.Provider
import com.eden.livewidget.logic.DestinationState
import kotlin.uuid.Uuid

@Composable
fun DestinationsGroup(
    provider: Provider,
    point: LogicalPoint,
    destinations: Map<Uuid, DestinationState>,
    addDestination: (DestinationConfig) -> Unit,
    updateDestination: (Uuid, DestinationConfig) -> Unit,
    removeDestination: (Uuid) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        destinations.onEachIndexed { index, (id, filter) ->
            DestinationItem(
                provider = provider,
                point = point,
                destination = filter,
                updateDestination = { newFilter -> updateDestination(id, newFilter) },
                removeDestination = { removeDestination(id) },
                shapes = ListItemDefaults.segmentedShapes(
                    index,
                    destinations.size + 1,
                ),
            )
        }

        AddDestinationItem(
            provider = provider,
            point = point,
            addDestination = addDestination,
            shapes = ListItemDefaults.segmentedShapes(
                destinations.size,
                (destinations.size) + 1
            ),
        )
    }
}

