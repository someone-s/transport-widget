package com.eden.livewidget.ui.main.datasync.key

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.data.agency.common.keys.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SourceKeyConfigItems(
    agency: Agency,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    var isConfigureExpanded by remember { mutableStateOf(false) }

    ListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_shared_outlined_key),
                contentDescription = null
            )
        },
        content = {
            Text(
                text = stringResource(R.string.data_sync_screen_key_configure_expand_button_main_text)
            )
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.data_sync_screen_key_configure_expand_button_support_text)
            )
        },
        trailingContent = {
            if (isConfigureExpanded)
                Icon(
                    painter = painterResource(R.drawable.ic_shared_arrow_drop_up),
                    contentDescription = stringResource(R.string.shared_arrow_drop_up_icon)
                )
            else
                Icon(
                    painter = painterResource(R.drawable.ic_shared_arrow_drop_down),
                    contentDescription = stringResource(R.string.shared_arrow_drop_down_icon)
                )
        },
        colors =
            if (isConfigureExpanded)
                ListItemDefaults.colors()
            else
                ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = {
            isConfigureExpanded = !isConfigureExpanded
        }
    )

    AnimatedVisibility(
        visible = isConfigureExpanded,
        enter =
            slideInVertically() +
                    expandVertically() +
                    fadeIn(),
        exit =
            slideOutVertically() +
                    shrinkVertically() +
                    fadeOut()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SourceKeyHelpItem(agency)

            val keys = remember { mutableStateListOf<Key>() }
            LaunchedEffect(agency) {
                withContext(Dispatchers.IO) {
                    keys.clear()
                    keys.addAll(agency.provider.keys)
                }
            }

            keys.forEach { (purpose, keyProvider) ->

                val key by keyProvider.key.collectAsState(null)

                SourceKeyOpenItem(
                    purpose,
                    keyValue = key?.ifBlank { null } ?: stringResource(R.string.data_sync_screen_key_configure_value_empty_text),
                    setKey = { keyProvider.setKey(it) }
                )

                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        keyProvider.refreshKey()
                    }
                }
            }
        }
    }
}