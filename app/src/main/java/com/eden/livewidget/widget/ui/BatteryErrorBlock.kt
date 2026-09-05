package com.eden.livewidget.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.eden.livewidget.widget.util.getResolvedBatterySaverSettingsIntent
import com.eden.livewidget.widget.util.getResolvedAppSettingsIntent

@Composable
fun BatteryErrorBlock() {
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
                    text = LocalContext.current.getString(R.string.widget_retry_battery_reason_text),
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

    val appSettingsIntent = getResolvedAppSettingsIntent(LocalContext.current.packageManager, LocalContext.current.packageName)

    val batterySaverSettingsIntent = getResolvedBatterySaverSettingsIntent(LocalContext.current.packageManager)

    Row(
        modifier = GlanceModifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_settings_applications),
            text = LocalContext.current.getString(R.string.widget_retry_battery_disable_optimization_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(appSettingsIntent)
        )
        Image(
            provider = ImageProvider(R.drawable.ic_shared_outlined_arrow_outward),
            contentDescription = LocalContext.current.getString(R.string.widget_retry_option_icon),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
        FilledButton(
            icon = ImageProvider(R.drawable.ic_shared_outlined_energy_savings_leaf),
            text = LocalContext.current.getString(R.string.widget_retry_battery_disable_saver_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(batterySaverSettingsIntent),
        )
    }
}