package com.eden.livewidget.main.ui.datasync

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.eden.livewidget.R

@Composable
fun PlaceWidgetGuide(
    setPlaceGuideState: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {
                setPlaceGuideState(false)
            },
            confirmButton = {
                Button(
                    onClick = {
                        setPlaceGuideState(false)
                    }
                ) { Text(stringResource(R.string.data_sync_place_widget_dismiss)) }
            },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_data_sync_place_widget_icon),
                    stringResource(R.string.data_sync_place_widget_icon_description)
                )
            },
            title = { Text(stringResource(R.string.data_sync_place_widget_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.data_sync_place_widget_body,
                        stringResource(R.string.app_name)
                    )
                )
            }
        )
    }
}