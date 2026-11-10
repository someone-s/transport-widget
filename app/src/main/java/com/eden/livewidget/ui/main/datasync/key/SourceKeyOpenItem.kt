package com.eden.livewidget.ui.main.datasync.key

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R
import com.eden.livewidget.data.agency.common.keys.KeyPurpose
import java.util.EnumSet

@Composable
fun SourceKeyOpenItem(
    purpose: EnumSet<KeyPurpose>,
    keyValue: String,
    setKey: (newKeyValue: String) -> Unit
) {

    var isInputVisible by remember { mutableStateOf(false) }

    ListItem(
        shapes = ListItemDefaults.shapes(
            shape = MaterialTheme.shapes.medium
        ),
        leadingContent = {
            Box(Modifier.size(24.dp)) {}
        },
        content = {
            Text(
                text =
                    if (purpose.contains(KeyPurpose.POINTS) and purpose.contains((KeyPurpose.ARRIVALS)))
                        stringResource(R.string.data_sync_screen_key_configure_purpose_point_and_arrivals_text)
                    else if (purpose.contains(KeyPurpose.POINTS))
                        stringResource(R.string.data_sync_screen_key_configure_purpose_point_text)
                    else if (purpose.contains(KeyPurpose.ARRIVALS))
                        stringResource(R.string.data_sync_screen_key_configure_purpose_arrivals_text)
                    else
                        stringResource(R.string.data_sync_screen_key_configure_purpose_unknown_text)
            )
        },
        supportingContent = { Text(text = keyValue) },
        onClick = { isInputVisible = true }
    )

    if (isInputVisible)
        KeyInputDialog(
            currentKeyValue = keyValue,
            onDismiss = { isInputVisible = false },
            onConfirm = {
                setKey(it)
                isInputVisible = false
            }
        )
}