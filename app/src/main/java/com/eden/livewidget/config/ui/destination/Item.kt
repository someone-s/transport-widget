package com.eden.livewidget.config.ui.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R
import com.eden.livewidget.config.ui.common.SearchGroup
import com.eden.livewidget.config.ui.common.SearchOption
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.arrivals.filter.Status
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun Item(
    currentProvider: Provider?,
    currentPoint: Model?,
    filter: Filter,
    onFilterChange: (Filter?) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {

    val context = LocalContext.current

    // Avoid standard rememberSwipeToDismissBoxState as that does not have key option
    // Also do not need to survive recomposition
    val positionThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val dismissBoxState = remember(filter) {
        SwipeToDismissBoxState(
            SwipeToDismissBoxValue.Settled,
            positionThreshold,
        )
    }

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()

    val repository = remember(key1 = currentProvider) {
        if (currentProvider != null) Repository.create(context, currentProvider)
        else null
    }
    val matchingPointsFlow = repository?.matchingPoints ?: MutableStateFlow(emptyList())
    val matchingPoints by matchingPointsFlow.collectAsState()

    LaunchedEffect(repository, textFieldState) {
        snapshotFlow { Pair(repository, textFieldState.text) }
            .collect { (repository, query) ->
                repository?.fetchMatching(context, query.toString())
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
                        queryText = point.model.name,
                        supportText = point.annotation
                    )
                },
            onOptionClick = { index, _ ->
                if (index >= 0 && index < matchingPoints.size && currentPoint != null)
                    onFilterChange(
                        Filter.createPending(
                            fromPoint = currentPoint,
                            toPoint = matchingPoints[index].model,
                        )
                    )
            }
        )
        SwipeToDismissBox(
            state = dismissBoxState,
            backgroundContent = {
                when (dismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        ItemDeleteBackground(Alignment.CenterStart, shapes)
                    SwipeToDismissBoxValue.EndToStart ->
                        ItemDeleteBackground(Alignment.CenterEnd, shapes)
                    SwipeToDismissBoxValue.Settled -> {}
                }
            },
            onDismiss = {
                onFilterChange(null)
            }, content = {
                ItemContent(
                    filter = filter,
                    onClick = {
                        if (filter.status == Status.APPLIED)
                            coroutineScope.launch {
                                textFieldState.setTextAndPlaceCursorAtEnd(filter.toPoint.name)
                                searchBarState.animateToExpanded()
                            }
                    },
                    shapes
                )
            }
        )
    }
}

