package com.eden.livewidget.widget.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.glance.appwidget.action.actionStartActivity
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
fun MeteredErrorBlock() {
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
                    text = LocalContext.current.getString(R.string.widget_retry_metered_reason_text),
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

    val networkSettingsIntent = getResolvedNetworkSettingsIntent(LocalContext.current.packageManager)

    val meteredSettingsIntent = getResolvedMeteredSettingsIntent(LocalContext.current.packageManager, LocalContext.current.packageName)

    Row(
        modifier = GlanceModifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_data_usage),
            text = LocalContext.current.getString(R.string.widget_retry_metered_data_saver_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(networkSettingsIntent)
        )
        Image(
            provider = ImageProvider(R.drawable.ic_shared_outlined_arrow_outward),
            contentDescription = LocalContext.current.getString(R.string.widget_retry_option_icon),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_data_usage),
            text = LocalContext.current.getString(R.string.widget_retry_metered_add_whitelist_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(meteredSettingsIntent),
        )
    }
}

private fun getResolvedNetworkSettingsIntent(packageManager: PackageManager): Intent {

    val networkSettingsIntent = Intent().apply {

            action =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    Settings.ACTION_DATA_USAGE_SETTINGS
                else
                    Settings.ACTION_SETTINGS

    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val networkSettingsComponentName =
        networkSettingsIntent.resolveActivity(packageManager)

    return networkSettingsIntent.apply {
        component = networkSettingsComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}

private fun getResolvedMeteredSettingsIntent(packageManager: PackageManager, packageName: String): Intent {
    val meteredSettingsIntent = Intent().apply {
        action = Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS
        data = "package:$packageName".toUri()
    }

    // Settings app should be visible by default
    @SuppressLint("QueryPermissionsNeeded")
    val meteredSettingsComponentName =
        meteredSettingsIntent.resolveActivity(packageManager)

    return meteredSettingsIntent.apply {
        component = meteredSettingsComponentName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}