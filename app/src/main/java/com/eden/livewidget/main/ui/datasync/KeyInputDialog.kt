package com.eden.livewidget.main.ui.datasync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R

@Composable
fun KeyInputDialog(
    currentKeyValue: String,
    setInputKeyState: (Boolean) -> Unit,
    currentInputKeyAction: ((key: String) -> Unit)?,
) {
    val textFieldState = rememberTextFieldState(initialText = currentKeyValue)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setInputKeyState(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentInputKeyAction != null)
                            currentInputKeyAction(textFieldState.text.toString())
                        setInputKeyState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_download_warning_dialog_confirm)) }
            },
            dismissButton = {
                Button(onClick = {
                    setInputKeyState(false)
                }) { Text(stringResource(R.string.data_sync_download_warning_dialog_cancel)) }
            },
            title = { Text(stringResource(R.string.data_sync_key_input_dialog_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        label = {
                            Text(stringResource(R.string.data_sync_key_input_field_label))
                        },
                        state = textFieldState,

                    )
                }
            }
        )
    }
}