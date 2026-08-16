package com.eden.livewidget.config.ui.destination

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.data.common.filter.Status
import com.eden.livewidget.data.common.filter.destination.Filter

@Composable
fun ItemContent(
    filter: Filter,
    onClick: () -> Unit,
    shapes: ListItemShapes,
) {

    SegmentedListItem(
        shapes = shapes,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.directions_alt),
                contentDescription = null,
            )
        },
        content = {
            Text(
                text = filter.toPoint.name
            )
        },
        supportingContent = {
            if (filter.status != Status.APPLIED) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onTertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary,
                )
            } else {
                Text(
                    text = filter.toPoint.apiValue
                )
            }
        },
        trailingContent = {
            Icon(
                painter = painterResource(R.drawable.edit),
                contentDescription = stringResource(R.string.config_ui_destination_itemcontent_action),
            )
        },
        onClick = onClick,
    )
}