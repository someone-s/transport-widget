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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.data.Provider
import com.eden.livewidget.ui.component.CustomizableSearchBar
import com.eden.livewidget.ui.theme.TransportWidgetsTheme
import me.xdrop.fuzzywuzzy.FuzzySearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguratorSelectProviderScreen(
    navController: NavController,
    setApiProvider: (Provider) -> Unit
) {

    // Controls expansion state of the search bar
    val textFieldState = rememberTextFieldState()

    val agencyLookup: Map<String, Agency> =
        Agency.entries.associateBy { agency -> stringResource(agency.agencyName) }

    var topAgencyMatch = remember { listOf<String>() }



    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomizableSearchBar(
                    onQueryChange = {
                        textFieldState.edit { replace(0, length, it) }
                        val results = FuzzySearch.extractTop(it, agencyLookup.keys, 10)
                        topAgencyMatch = results.map { result -> result.string }
                    },
                    query = textFieldState.text.toString(),
                    onSearch = { },
                    searchResults = topAgencyMatch,
                    onResultClick = { index, _ ->
                        if (index >= topAgencyMatch.size) return@CustomizableSearchBar

                        setApiProvider((agencyLookup[topAgencyMatch[index]] as Agency).apiProvider)
                        navController.navigate(SelectPoint)
                    },
                    placeholder = { Text(stringResource(R.string.configure_point_screen_search_bar_placeholder)) },
                    supportingContent = { index, _ ->
                        if (index >= topAgencyMatch.size) return@CustomizableSearchBar
                        Text(
                            text = stringResource((agencyLookup[topAgencyMatch[index]] as Agency).agencyShortDescription),
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
                horizontalAlignment = Alignment.CenterHorizontally) {
                itemsIndexed(Agency.entries) { index, agency ->
                    Surface(
                        modifier = Modifier.widthIn(0.dp, 360.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(
                            topStart = if (index == 0) 16.dp else 2.dp,
                            topEnd = if (index == 0) 16.dp else 2.dp,
                            bottomStart = if (index == Agency.entries.size - 1) 16.dp else 2.dp,
                            bottomEnd = if (index == Agency.entries.size - 1) 16.dp else 2.dp
                        ),

                        ) {

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(agency.agencyName),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = LocalTextStyle.current
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(agency.agencyShortDescription),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .clickable {
                                    setApiProvider(agency.apiProvider)
                                    navController.navigate(SelectPoint)
                                }
                                .fillMaxWidth())
                    }

                }
            }
        },
    )
}

@PreviewScreenSizes
@Composable
fun PreviewConfiguratorSelectProviderScreen() {

    TransportWidgetsTheme {

        ConfiguratorSelectProviderScreen(rememberNavController()) { _ -> }
    }
}