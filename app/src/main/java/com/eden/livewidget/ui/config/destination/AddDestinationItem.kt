package com.eden.livewidget.ui.config.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eden.livewidget.R
import com.eden.livewidget.ui.config.common.SearchGroup
import com.eden.livewidget.ui.config.common.SearchOption
import com.eden.livewidget.data.agency.common.destination.DestinationConfig
import com.eden.livewidget.data.agency.common.point.LogicalPoint
import com.eden.livewidget.data.agency.common.Provider
import kotlinx.coroutines.launch

@Composable
fun AddDestinationItem(
    provider: Provider,
    point: LogicalPoint?,
    addDestination: (DestinationConfig) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
    viewModel: DestinationsViewModel =
        if (LocalInspectionMode.current) mockDestinationsViewModel
        else viewModel(
            key = provider::class.qualifiedName,
            factory = DestinationsViewModelFactory(provider)
        ),
) {
    val destinationsUiState by viewModel.destinationsUiState.collectAsState()
    val matchingPoints = destinationsUiState.matchingPoints

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
            placeholder = stringResource(R.string.config_ui_destination_addbutton_input_placeholder),
            options = matchingPoints
                .take(25)
                .map { point ->
                    SearchOption(
                        queryText = point.logicalPoint.name,
                        supportText = point.annotation
                    )
                },
            onOptionClick = { index, _ ->
                if (index >= 0 && index < matchingPoints.size && point != null)
                    addDestination(
                        DestinationConfig(
                            fromPoint = point,
                            toPoint = matchingPoints[index].logicalPoint,
                        )
                    )
            }
        )

        SegmentedListItem(
            shapes = shapes,
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = null,
                )
            },
            content = {
                Text(
                    text = stringResource(R.string.config_ui_destination_addbutton_action)
                )
            },
            onClick = {
                coroutineScope.launch {
                    textFieldState.setTextAndPlaceCursorAtEnd("")
                    searchBarState.animateToExpanded()
                }
            }
        )
    }
}