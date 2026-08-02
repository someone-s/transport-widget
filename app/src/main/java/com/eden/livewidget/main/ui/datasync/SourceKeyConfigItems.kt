package com.eden.livewidget.main.ui.datasync

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.data.keys.KeyPurpose

@Composable
fun SourceKeyConfigItems(
    context: Context?,
    agency: Agency,
    setCurrentInputKeyAction: (((String) -> Unit)?) -> Unit,
    setCurrentKeyValue: (String) -> Unit,
    setInputKeyState: (Boolean) -> Unit
) {
    var isConfigureExpanded by remember { mutableStateOf(false) }

    ListItem(
        shapes = ListItemDefaults.shapes(
            shape = MaterialTheme.shapes.medium
        ),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_data_sync_key),
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

        agency.apiProvider.keyProviders.forEach { (purpose, constructor) ->
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
                ListItem(
                    shapes = ListItemDefaults.shapes(
                        shape = MaterialTheme.shapes.medium
                    ),
                    leadingContent = {
                        Box(Modifier.size(24.dp)) {}
                    },
                    content = {
                        Text(
                            text =
                                if (purpose.contains(KeyPurpose.POINTS) and purpose.contains((KeyPurpose.ARRIVALS)))
                                    stringResource(R.string.data_sync_screen_key_configure_purpose_point_and_arrivals_text)
                                else if (purpose.contains(KeyPurpose.POINTS))
                                    stringResource(R.string.data_sync_screen_key_configure_purpose_point_text)
                                else if (purpose.contains(KeyPurpose.ARRIVALS))
                                    stringResource(R.string.data_sync_screen_key_configure_purpose_arrivals_text)
                                else
                                    stringResource(R.string.data_sync_screen_key_configure_purpose_unknown_text)


                        )
                    },
                    supportingContent = {
                        Text(
                            text =
                                if (context != null)
                                    constructor().getKey(context)
                                        .ifBlank { stringResource(R.string.data_sync_screen_key_configure_value_empty_text) }
                                else
                                    stringResource(R.string.data_sync_screen_key_configure_value_empty_text)
                        )
                    },
                    onClick = {
                        if (context == null) return@ListItem
                        setCurrentInputKeyAction({ newKey ->
                            constructor().setKey(
                                context,
                                newKey
                            )
                        })
                        setCurrentKeyValue(constructor().getKey(context))
                        setInputKeyState(true)
                    }
                )
            }
    }
}