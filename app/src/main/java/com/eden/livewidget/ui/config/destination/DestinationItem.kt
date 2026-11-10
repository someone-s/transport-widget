package com.eden.livewidget.ui.config.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.R
import com.eden.livewidget.ui.common.components.SearchGroup
import com.eden.livewidget.ui.common.components.SearchOption
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.point.FixedLogicalPoint
import com.eden.livewidget.data.agency.common.Provider
import com.eden.livewidget.logic.DestinationState
import kotlinx.coroutines.launch

@Composable
fun DestinationItem(
    provider: Provider,
    point: FixedLogicalPoint,
    destination: DestinationState,
    updateDestination: (DestinationConfig) -> Unit,
    removeDestination: () -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    viewModel: DestinationsViewModel =
        if (LocalInspectionMode.current) mockDestinationsViewModel
        else viewModel(
            key = provider::class.qualifiedName,
            factory = DestinationsViewModelFactory(provider)
        ),
) {
    val destinationsUiState by viewModel.destinationsUiState.collectAsState()
    val matchingPoints = destinationsUiState.matchingOptions

    // Avoid standard rememberSwipeToDismissBoxState as that does not have key option
    // Also do not need to survive recomposition
    val positionThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(destination) {
        SwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            positionThreshold,
        )
    }

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(provider, textFieldState.text) {
        coroutineScope.launch {
            viewModel.fetchMatching(textFieldState.text.toString())
        }
    }


    Box {
        SearchGroup(
            searchBarState = searchBarState,
            textFieldState = textFieldState,
            placeholder = stringResource(R.string.config_ui_destination_item_input_placeholder),
            options = matchingPoints
                .take(25)
                .map { point ->
                    SearchOption(
                        queryText = point.fixedLogicalPoint.nameString,
                        supportText = point.annotation
                    )
                },
            onOptionClick = { index, _ ->
                if (index >= 0 && index < matchingPoints.size)
                    updateDestination(
                        DestinationConfig(
                            fromPoint = point,
                            toPoint = matchingPoints[index].fixedLogicalPoint,
                        )
                    )
            }
        )
        SwipeToDismissBox(
            state = dismissBoxState,
            backgroundContent = {
                when (dismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        DestinationItemDeleteBackground(Alignment.CenterStart, shapes)
                    SwipeToDismissBoxValue.EndToStart ->
                        DestinationItemDeleteBackground(Alignment.CenterEnd, shapes)
                    SwipeToDismissBoxValue.Settled -> {}
                }
            },
            onDismiss = {
                removeDestination()
            }, content = {
                DestinationItemContent(
                    destination = destination,
                    onClick = {
                        if (destination.filters != null)
                            coroutineScope.launch {
                                textFieldState.setTextAndSelectAll(destination.config.toPoint.nameString)
                                searchBarState.animateToExpanded()
                            }
                    },
                    shapes
                )
            }
        )
    }
}

