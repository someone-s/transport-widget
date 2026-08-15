package com.eden.livewidget.config.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eden.livewidget.R

@Composable
fun ApplyButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = stringResource(R.string.config_ui_applybutton_action),
                )
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.save),
                    contentDescription = null,
                )
            },
            onClick = onClick,
            modifier = Modifier.padding(16.dp)
        )

    }
}