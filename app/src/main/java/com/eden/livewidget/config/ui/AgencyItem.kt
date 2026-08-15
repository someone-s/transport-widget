package com.eden.livewidget.config.ui

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import kotlinx.coroutines.launch
import me.xdrop.fuzzywuzzy.FuzzySearch

@Composable
fun AgencyItem(
    currentAgency: Agency?,
    onAgencySelected: (Agency?) -> Unit,
) {

    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val coroutineScope = rememberCoroutineScope()

    val agencyLookup: Map<String, Agency> =
        Agency.entries.associateBy { option -> stringResource(option.agencyName) }

    val currentAgencyName = stringResource(currentAgency?.agencyName ?: R.string.config_ui_agencyitem_name_placeholder)
    val currentAgencyShortDescription = stringResource(currentAgency?.agencyShortDescription ?: R.string.config_ui_agencyitem_description_placeholder)

    val results by remember {
        derivedStateOf {
            FuzzySearch
                .extractTop(textFieldState.text.toString(), agencyLookup.keys, 10)
                ?.filterNotNull()
                ?.mapNotNull { result -> result.string }
                ?: emptyList()
        }
    }

    SearchGroup(
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        placeholder = stringResource(R.string.config_ui_agencyitem_input_placeholder),
        results,
        onResultClick = { _, result ->
            onAgencySelected(agencyLookup[result])
        }
    )

    ListItem(
        shapes = ListItemDefaults.shapes(
            shape = MaterialTheme.shapes.medium,
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_shared_outlined_corporate_fare),
                contentDescription = null,
            )
        },
        content = {
            Text(
                text = currentAgencyName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = currentAgencyShortDescription,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.swap_horiz),
                contentDescription = stringResource(R.string.config_ui_agencyitem_action),
            )
        },
        onClick = {
            coroutineScope.launch {
                textFieldState.setTextAndPlaceCursorAtEnd(currentAgencyName)
                searchBarState.animateToExpanded()
            }
        }
    )
}

