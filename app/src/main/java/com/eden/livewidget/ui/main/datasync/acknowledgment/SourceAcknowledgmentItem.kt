package com.eden.livewidget.ui.main.datasync.acknowledgment

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency

@Composable
fun SourceAcknowledgmentItem(
    agency: Agency,
    shapes: ListItemShapes = ListItemDefaults.shapes()
) {

    val context = LocalContext.current

    ListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.copyright),
                contentDescription = null
            )
        },
        content = {
            Text(text = stringResource(R.string.ui_main_datasync_sourceacknowledgmentitem_title))
        },
        supportingContent = {
            Text(text = stringResource(R.string.ui_main_datasync_sourceacknowledgmentitem_support))
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, agency.agencyHelp)
        }
    )
}