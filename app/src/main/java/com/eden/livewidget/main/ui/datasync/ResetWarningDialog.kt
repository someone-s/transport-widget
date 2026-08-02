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
fun ResetWarningDialog(
    setResetWarningState: (Boolean) -> Unit,
    currentResetAction: (() -> Unit)?,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setResetWarningState(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentResetAction != null)
                            currentResetAction()
                        setResetWarningState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_reset_warning_dialog_confirm)) }
            },
            dismissButton = {
                Button(onClick = {
                    setResetWarningState(false)
                }) { Text(stringResource(R.string.data_sync_reset_warning_dialog_cancel)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_reset),
                    stringResource(R.string.data_sync_reset_warning_dialog_icon_description)
                )
            },
            title = { Text(stringResource(R.string.data_sync_reset_warning_title)) },
            text = { Text(stringResource(R.string.data_sync_reset_warning_body)) }
        )
    }
}