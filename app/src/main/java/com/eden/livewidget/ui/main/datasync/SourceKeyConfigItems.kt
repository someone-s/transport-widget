package com.eden.livewidget.ui.main.datasync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R
import com.eden.livewidget.data.Agency
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SourceKeyConfigItems(
    agency: Agency,
    setCurrentInputKeyAction: (((String) -> Unit)?) -> Unit,
    setCurrentKeyValue: (String) -> Unit,
    setInputKeyState: (Boolean) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val context = LocalContext.current
    var isConfigureExpanded by remember { mutableStateOf(false) }

    ListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_shared_outlined_key),
                contentDescription = stringResource(R.string.data_sync_screen_key_configure_expand_button_icon)
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
        SourceKeyHelpItem(agency)
    }

    agency.provider.keys.forEach { (purpose, provider) ->
        var shouldReloadKey by remember { mutableStateOf(false) }
        val keyTextFlow = remember { MutableStateFlow<String?>(null) }
        val keyText by keyTextFlow.collectAsState()

        LaunchedEffect(shouldReloadKey) {
            keyTextFlow.emit(provider.getKey(context))
        }

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
            SourceKeyOpenItem(
                purpose,
                keyText = keyText?.ifBlank { null } ?: stringResource(R.string.data_sync_screen_key_configure_value_empty_text),
                onClick = {
                    setCurrentInputKeyAction { newKey ->
                        provider.setKey(context, newKey)
                        shouldReloadKey = !shouldReloadKey
                    }
                    setCurrentKeyValue(provider.getKey(context))
                    setInputKeyState(true)
                }
            )
        }
    }
}