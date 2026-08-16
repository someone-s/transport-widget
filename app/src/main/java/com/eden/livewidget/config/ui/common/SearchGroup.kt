package com.eden.livewidget.config.ui.common

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import com.eden.livewidget.R
import kotlinx.coroutines.launch


/**
 * Create full screen search bar and return zero height search bar to allow animation from desired location
 *
 * @
 */
@Composable
fun SearchGroup(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    placeholder: String = "",
    results: List<String>,
    onResultClick: (index: Int, text: String) -> Unit,
) {
    val resultScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { coroutineScope.launch { searchBarState.animateToCollapsed() } },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_shared_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = null
            )
        }

    HiddenSearchBar(
        state = searchBarState,
        inputField = inputField,
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {
        LazyColumn(
            modifier = Modifier
                .verticalScroll(resultScrollState)
                .heightIn(max = LocalWindowInfo.current.containerDpSize.height)
        ) {
            itemsIndexed(results) { index, result ->
                ListItem(
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(result)
                        onResultClick(index, result)
                        coroutineScope.launch { searchBarState.animateToCollapsed() }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                ) {
                    Text(text = result)
                }
            }
        }
    }
}