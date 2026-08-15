package com.eden.livewidget.main.ui.datasync

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.Agency
import com.eden.livewidget.R
import com.eden.livewidget.main.DataSyncWorker
import kotlinx.coroutines.flow.flow

@Composable
fun SourceDownloadItems(
    context: Context?,
    agency: Agency,
    setCurrentDownloadAction: ((() -> Unit)?) -> Unit,
    setDownloadWarningState: (Boolean) -> Unit,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val flow = if (context != null) DataSyncWorker.getIsActiveFlow(
        context,
        agency.apiProvider
    ) else flow { }
    val fetching by flow.collectAsState(true)

    ListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_data_sync_download),
                contentDescription = stringResource(R.string.data_sync_download_warning_dialog_icon_description)
            )
        },
        content = {
            Text(
                text = stringResource(R.string.data_sync_screen_download_data_button_main_text)
            )
        },
        supportingContent = {
            if (fetching) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onTertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary,
                )
            }
            else {
                Text(
                    text = stringResource(R.string.data_sync_screen_download_data_button_support_text)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = {
            if (context == null) return@ListItem
            if (fetching) return@ListItem
            setCurrentDownloadAction {
                DataSyncWorker.schedule(context, agency.apiProvider)
            }
            setDownloadWarningState(true)
        }
    )
}