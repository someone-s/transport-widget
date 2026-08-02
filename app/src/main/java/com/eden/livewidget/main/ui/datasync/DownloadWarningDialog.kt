package com.eden.livewidget.main.ui.datasync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R

@Composable
fun DownloadWarningDialog(
    setDownloadWarningState: (Boolean) -> Unit,
    currentDownloadAction: (() -> Unit)?,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setDownloadWarningState(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentDownloadAction != null)
                            currentDownloadAction()
                        setDownloadWarningState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_download_warning_dialog_confirm)) }
            },
            dismissButton = {
                Button(onClick = {
                    setDownloadWarningState(false)
                }) { Text(stringResource(R.string.data_sync_download_warning_dialog_cancel)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_download),
                    stringResource(R.string.data_sync_download_warning_dialog_icon_description)
                )
            },
            title = { Text(stringResource(R.string.data_sync_download_warning_dialog_title)) },
            text = { Text(stringResource(R.string.data_sync_download_warning_dialog_body)) }
        )
    }
}