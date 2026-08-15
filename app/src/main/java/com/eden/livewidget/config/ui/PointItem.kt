package com.eden.livewidget.config.ui

import android.util.Log
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
import com.eden.livewidget.config.PointInfo
import com.eden.livewidget.data.Provider
import com.eden.livewidget.data.points.PointsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun PointItem(
    currentProvider: Provider?,
    currentPoint: PointInfo?,
    onPointSelected: (PointInfo?) -> Unit,
) {
    val context = LocalContext.current

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()

    val repository = remember(key1 = currentProvider) {
        if (currentProvider != null)
            PointsRepository.getInstance(context, currentProvider)
        else
            null
    }
    val matchingPointsFlow = repository?.matchingPoints ?: MutableStateFlow(emptyList())
    val matchingPoints by matchingPointsFlow.collectAsState()

    Log.i(context.packageName, "Recompose ${currentProvider != null} ${repository != null}")

    LaunchedEffect(repository, textFieldState) {
        snapshotFlow { Pair(repository, textFieldState.text) }
            .collect { (repository, query) ->
                Log.i(context.packageName, (currentProvider != null).toString())
                repository?.fetchMatching(query.toString())
            }
    }

    SearchGroup(
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        placeholder = stringResource(R.string.config_ui_pointitem_input_placeholder),
        results = matchingPoints.take(25).map { point -> point.name },
        onResultClick = { index, _ ->
            onPointSelected(
                if (index >= 0 && index < matchingPoints.size)
                    PointInfo(
                        name = matchingPoints[index].name,
                        apiValue = matchingPoints[index].apiValue,
                    )
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
                text = currentPoint?.apiValue ?: stringResource(R.string.config_ui_pointitem_description_placeholder),
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