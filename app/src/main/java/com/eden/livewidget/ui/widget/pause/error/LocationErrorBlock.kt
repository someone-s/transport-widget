package com.eden.livewidget.ui.widget.pause.error

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
import com.eden.livewidget.ui.util.getResolvedAppSettingsIntent
import com.eden.livewidget.ui.util.getResolvedLocationSettingsIntent

@Composable
fun LocationErrorBlock(
    appWidgetId: Int,
) {
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
            RetrySurface(
                appWidgetId = appWidgetId,
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.widget_retry_location_reason_text),
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
                UpdateLocationSettingsGroup()
            }
        }
    }
}

@Composable
private fun UpdateLocationSettingsGroup() {

    val appSettingsIntent = getResolvedAppSettingsIntent(LocalContext.current.packageManager, LocalContext.current.packageName)

    val locationSettingsIntent = getResolvedLocationSettingsIntent(LocalContext.current.packageManager)

    Row(
        modifier = GlanceModifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledButton(
            icon = ImageProvider(R.drawable.security),
            text = LocalContext.current.getString(R.string.widget_retry_location_grant_fine_location_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(appSettingsIntent)
        )
        Image(
            provider = ImageProvider(R.drawable.add),
            contentDescription = LocalContext.current.getString(R.string.widget_retry_option_icon),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
        FilledButton(
            icon = ImageProvider(R.drawable.location_off),
            text = LocalContext.current.getString(R.string.widget_retry_location_enable_text),
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .fillMaxHeight(),
            maxLines = 2,
            onClick = actionStartActivity(locationSettingsIntent),
        )
    }
}