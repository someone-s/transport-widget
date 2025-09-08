package com.eden.livewidget.widget.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
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
fun ConfigurationBatteryPrompt(context: Context, setVisible: (Boolean) -> Unit) {


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = {setVisible(false) },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent().apply {
                            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                        }
                        context.startActivity(intent)
                        setVisible(false)
                    }
                ) { Text(text = stringResource(R.string.configure_battery_prompt_confirm_text)) }
            },
            dismissButton = {
                TextButton( onClick = { setVisible(false) } ) {  Text( text = stringResource(R.string.configure_battery_prompt_dismiss_text)) }
            },
            icon = { Icon(painterResource(R.drawable.ic_alert_battery),
                stringResource(R.string.configure_battery_prompt_icon_description)
            ) },
            title = { Text(stringResource(R.string.configure_battery_prompt_explain_title)) },
            text = { Text(stringResource(R.string.configure_battery_prompt_explain_body)) }
        )
    }
}