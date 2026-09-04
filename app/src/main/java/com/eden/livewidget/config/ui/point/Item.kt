package com.eden.livewidget.config.ui.point

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import com.eden.livewidget.R
import com.eden.livewidget.config.ui.common.SearchGroup
import com.eden.livewidget.config.ui.common.SearchOption
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.common.points.Model
import com.eden.livewidget.data.common.points.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun Item(
    currentProvider: Provider?,
    currentPoint: Model?,
    onPointSelected: (Model?) -> Unit,
) {
    val context = LocalContext.current

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()

    val repository = remember(key1 = currentProvider) {
        if (currentProvider != null)
            Repository.create(context, currentProvider)
        else
            null
    }
    val matchingPointsFlow = repository?.matchingPoints ?: MutableStateFlow(emptyList())
    val matchingPoints by matchingPointsFlow.collectAsState()

    LaunchedEffect(repository, textFieldState) {
        snapshotFlow { Pair(repository, textFieldState.text) }
            .collect { (repository, query) ->
                repository?.fetchMatching(context, query.toString())
            }
    }

    SearchGroup(
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        placeholder = stringResource(R.string.config_ui_pointitem_input_placeholder),
        options = matchingPoints.take(25).map { SearchOption(queryText = it.model.name, supportText = it.annotation) },
        onOptionClick = { index, _ ->
            onPointSelected(
                if (index >= 0 && index < matchingPoints.size)
                    matchingPoints[index].model
                else
                    null
            )
        }
    )

    ListItem(
        shapes = ListItemDefaults.shapes(
            shape = MaterialTheme.shapes.medium
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.my_location),
                contentDescription = null,
            )
        },
        content = {
            Text(
                text = currentPoint?.name ?: stringResource(R.string.config_ui_pointitem_name_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = currentPoint?.values?.joinToString(",") { it.displayString }
                    ?: stringResource(R.string.config_ui_pointitem_description_placeholder),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.swap_horiz),
                contentDescription = stringResource(R.string.config_ui_pointitem_action),
            )
        },
        onClick = {
            coroutineScope.launch {
                textFieldState.setTextAndPlaceCursorAtEnd(currentPoint?.name ?: "")
                searchBarState.animateToExpanded()
            }
        }
    )
}