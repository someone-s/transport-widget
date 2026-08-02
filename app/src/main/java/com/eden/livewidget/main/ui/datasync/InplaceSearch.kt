package com.eden.livewidget.main.ui.datasync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch

@Composable
fun InplaceSearch(lazyListState: LazyListState) {
    // Controls expansion state of the search bar
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val resultScrollState = rememberScrollState()

    val agencyLookup: Map<String, Agency> =
        Agency.entries.associateBy { agency -> stringResource(agency.agencyName) }

    val coroutineScope = rememberCoroutineScope()


    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { coroutineScope.launch { searchBarState.animateToCollapsed() } },
                placeholder = { Text(stringResource(R.string.data_sync_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_shared_search),
                        contentDescription = "Search"
                    )
                },
                trailingIcon = null
            )
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
            ),
        contentAlignment = Alignment.Center
    ) {
        SearchBar(searchBarState, inputField)
        ExpandedFullScreenContainedSearchBar(searchBarState, inputField) {
            val results =
                FuzzySearch.extractTop(textFieldState.text.toString(), agencyLookup.keys, 10)

            Column(
                modifier = Modifier
                    .verticalScroll(resultScrollState)
                    .fillMaxHeight()
            ) {
                results.forEach { result ->
                    ListItem(
                        onClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd(result.string)
                            coroutineScope.launch { searchBarState.animateToCollapsed() }
                            coroutineScope.launch { lazyListState.animateScrollToItem(result.index) }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    ) {
                        Text(text = result.string)
                    }
                }
            }
        }
    }
}