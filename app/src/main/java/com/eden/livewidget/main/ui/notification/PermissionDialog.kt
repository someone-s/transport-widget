package com.eden.livewidget.main.ui.notification

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
fun PermissionDialog(onDismiss: () -> Unit, onClick: () -> Unit) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.notification_permission_dismiss_text)) }
            },
            confirmButton = {
                Button(
                    onClick = onClick
                ) { Text(text = stringResource(R.string.notification_permission_request_proceed_text)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_shared_filled_notification_active),
                    stringResource(R.string.notification_permission_icon_description)
                )
            },
            title = { Text(stringResource(R.string.notification_permission_explain_title)) },
            text = { Text(stringResource(R.string.notification_permission_explain_body)) }
        )
    }
}