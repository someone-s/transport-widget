package com.eden.livewidget.config.ui.destination

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R
import com.eden.livewidget.config.ui.common.SearchGroup
import com.eden.livewidget.config.ui.common.SearchOption
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.arrivals.filter.destination.Filter
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun AddButton(
    currentProvider: Provider?,
    currentPoint: Model?,
    onAddFilter: (Filter) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val context = LocalContext.current

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
            placeholder = stringResource(R.string.config_ui_destination_addbutton_input_placeholder),
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
                    onAddFilter(
                        Filter.createPending(
                            fromPoint = currentPoint,
                            toPoint = matchingPoints[index].model,
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