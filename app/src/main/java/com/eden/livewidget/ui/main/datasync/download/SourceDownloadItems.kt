package com.eden.livewidget.ui.main.datasync.download

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.Agency
import com.eden.livewidget.logic.sync.DataSyncWorker
import kotlinx.coroutines.flow.flow

@Composable
fun SourceDownloadItems(
    agency: Agency,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val context = LocalContext.current
    val flow =
        if (LocalInspectionMode.current) flow {  }
        else DataSyncWorker.getIsActiveFlow(context, agency)
    val fetching by flow.collectAsState(true)

    var isWarningVisible by remember { mutableStateOf(false) }

    ListItem(
        shapes = shapes,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_data_sync_download),
                contentDescription = null
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
            if (fetching) return@ListItem
            isWarningVisible = true
        }
    )

    if (isWarningVisible)
        DownloadWarningDialog(
            onDismiss = { isWarningVisible = false },
            onConfirm = {
                DataSyncWorker.schedule(context, agency)
                isWarningVisible = false
            }
        )

}