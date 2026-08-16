package com.eden.livewidget.config.ui.destination

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R

@Composable
fun ItemDeleteBackground(
    alignment: Alignment,
    shapes: ListItemShapes,
) {
    SegmentedListItem(
        shapes = shapes,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.tertiary,
        ),
        content = {
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = stringResource(R.string.config_ui_destination_itemdeletebackground_action),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(alignment),
            )
        }
    )
}