package com.eden.livewidget.ui.config.display

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.setTextAndSelectAll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.eden.livewidget.R
import com.eden.livewidget.data.widget.structure.ListStyle

private data class ResourceGroup(
    @StringRes
    val mainText: Int,
    @StringRes
    val supportText: Int,
)

private val styleResources = mapOf(
    ListStyle.SHOW_LATER_FULL to ResourceGroup(
        mainText = R.string.ui_config_display_liststyleitem_styleshowfull_main,
        supportText = R.string.ui_config_display_liststyleitem_styleshowfull_support,
    ),
    ListStyle.SHOW_LATER_GROUPED to ResourceGroup(
        mainText = R.string.ui_config_display_liststyleitem_styleshowgroup_main,
        supportText = R.string.ui_config_display_liststyleitem_styleshowgroup_support,
    ),
    ListStyle.HIDE_LATER to ResourceGroup(
        mainText = R.string.ui_config_display_liststyleitem_stylehide_main,
        supportText = R.string.ui_config_display_liststyleitem_stylehide_support,
    ),
)

@Composable
fun ListStyleItem(listStyle: ListStyle, onStyleSelected: (ListStyle) -> Unit) {

    val resourceGroup = styleResources[listStyle] ?: throw IllegalArgumentException("Unknown list style")

    var expanded by remember { mutableStateOf(false) }

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
                painter = painterResource(R.drawable.view_list),
                contentDescription = null,
            )
        },
        content = {
            Text(
                text = stringResource(resourceGroup.mainText),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                styleResources.forEach { (style, group) ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(group.mainText)) },
                        onClick = {
                            onStyleSelected(style)
                            expanded = false
                        }
                    )
                }
            }
        },
        supportingContent = {
            Text(
                text = stringResource(resourceGroup.supportText),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = stringResource(R.string.config_ui_agencyitem_action),
            )
        },
        onClick = {
            expanded = true
        }
    )
}