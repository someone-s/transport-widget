package com.eden.livewidget.main.ui.datasync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.main.util.getOpenUriIntent

@Composable
fun SourceKeyHelpItem(
    agency: Agency
) {

    val context = LocalContext.current

    ListItem(
        shapes = ListItemDefaults.shapes(
            shape = MaterialTheme.shapes.medium
        ),
        leadingContent = {
            Box(Modifier.size(24.dp)) {}
        },
        content = {
            Text(text = stringResource(R.string.data_sync_screen_key_configure_help_text))
        },
        supportingContent = {
            Text(text = stringResource(R.string.data_sync_screen_key_configure_help_support))
        },
        onClick = {
            context.startActivity(getOpenUriIntent(agency.agencyHelp))
        }
    )
}