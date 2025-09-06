package com.eden.livewidget.widget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.R
import com.eden.livewidget.data.points.PointsRepository
import com.eden.livewidget.data.utils.Provider
import com.eden.livewidget.ui.component.CustomizableSearchBar
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguratorSelectPointScreen(
    navController: NavHostController,
    createWidget: (apiProvider: Provider, apiValue: String, displayName: String) -> Unit,
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    // Controls expansion state of the search bar
    val textFieldState = rememberTextFieldState()

    val apiProvider = remember { mutableStateOf(Provider.TFL) }
    val repository =
        remember(key1 = apiProvider) { PointsRepository.getInstance(context, apiProvider.value) }
    val matchingPoints by repository.matchingPoints.collectAsState()

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CustomizableSearchBar(
                        onQueryChange = {
                            textFieldState.edit { replace(0, length, it) }
                            coroutineScope.launch {
                                val repository =
                                    PointsRepository.getInstance(context, apiProvider.value)
                                repository.fetchMatching(it)
                            }
                        },
                        query = textFieldState.text.toString(),
                        onSearch = {
                            coroutineScope.launch {
                                val repository =
                                    PointsRepository.getInstance(context, apiProvider.value)
                                repository.fetchMatching(it)
                            }
                        },
                        searchResults = matchingPoints.map { points -> points.name },
                        onResultClick = { index, _ ->
                            if (index >= matchingPoints.size)
                                return@CustomizableSearchBar

                            createWidget(matchingPoints[index].apiProvider, matchingPoints[index].apiValue, matchingPoints[index].name)
                        },
                        placeholder = { Text(stringResource(R.string.configure_point_screen_search_bar_placeholder)) },
                        supportingContent = { index, _ ->
                            if (index >= matchingPoints.size)
                                return@CustomizableSearchBar
                            if (matchingPoints[index].context == null)
                                return@CustomizableSearchBar
                            Text(
                                text = matchingPoints[index].context as String,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },

                        )
                }
        },
        content = { contentPadding ->

            LazyColumn(

                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .semantics {
                        traversalIndex = 1f
                    }
                    .padding(contentPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(matchingPoints) { index, item ->
                    Surface(
                        modifier = Modifier
                            .widthIn(0.dp, 360.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(
                            topStart = if (index == 0) 16.dp else 2.dp,
                            topEnd = if (index == 0) 16.dp else 2.dp,
                            bottomStart = if (index == matchingPoints.size - 1) 16.dp else 2.dp,
                            bottomEnd = if (index == matchingPoints.size - 1) 16.dp else 2.dp
                        ),

                        ) {

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = item.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = LocalTextStyle.current
                                )
                            },
                            supportingContent = {
                                if (item.context != null)
                                    Text(
                                        text = item.context,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.LocationOn,
                                    stringResource(R.string.configure_point_screen_location_icon_description)
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    createWidget(
                                        matchingPoints[index].apiProvider,
                                        matchingPoints[index].apiValue,
                                        matchingPoints[index].name
                                    )
                                }
                                .fillMaxWidth()
                        )
                    }

                }
            }
        },

    )

}

@PreviewScreenSizes
@Composable
fun PreviewConfiguratorSelectPointScreen() {

    TransportWidgetsTheme {

        ConfiguratorSelectPointScreen(rememberNavController(), {_, _, _ -> })
    }
}