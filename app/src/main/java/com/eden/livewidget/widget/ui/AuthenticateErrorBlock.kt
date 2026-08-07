package com.eden.livewidget.widget.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R

@Composable
fun AuthenticateErrorBlock() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            RetrySurface {
                Text(
                    text = LocalContext.current.getString(R.string.widget_retry_authenticate_reason_text),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }
        Box(
            modifier = GlanceModifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                UpdateKeyGroup()
            }
        }
    }
}

@Composable
private fun UpdateKeyGroup() {
    Row(
        modifier = GlanceModifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_filled_corporate_fare),
            text = LocalContext.current.getString(R.string.widget_retry_authenticate_go_to_service_text),
            onClick = {},
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2
        )
        Image(
            provider = ImageProvider(R.drawable.ic_shared_outlined_arrow_right_alt),
            contentDescription = LocalContext.current.getString(R.string.widget_retry_option_icon),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_key),
            text = LocalContext.current.getString(R.string.widget_retry_authenticate_update_key_text),
            onClick = {},
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2
        )
    }
}

private fun getResolvedProviderIntent(packageManager: PackageManager, packageName: String): Intent {

    val applicationSettingsIntent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val applicationSettingsComponentName =
        applicationSettingsIntent.resolveActivity(packageManager)

    return Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        component = applicationSettingsComponentName
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}

private fun getResolvedBatterySaverSettingsIntent(packageManager: PackageManager): Intent {
    val batterySaverSettingsIntent = Intent().apply {
        action = Settings.ACTION_BATTERY_SAVER_SETTINGS
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val batterySaverComponentName =
        batterySaverSettingsIntent.resolveActivity(packageManager)

    return batterySaverSettingsIntent.apply {
        component = batterySaverComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}