package com.eden.livewidget.ui.widget.pause.error

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.logic.update.UpdateService

@Composable
fun RetrySurface(
    appWidgetId: Int,
    reasonContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(12.dp)
            .padding(all = 8.dp)
            .clickable(
                onClick = actionStartService(
                    isForegroundService = true,
                    intent = Intent(UpdateService.ACTION_UPDATE).apply {
                        setClass(context, UpdateService::class.java)
                        putExtra(UpdateService.EXTRA_APP_WIDGET_ID, appWidgetId)
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = GlanceModifier.size(48.dp),
                provider = ImageProvider(R.drawable.ic_shared_outlined_replay),
                contentDescription = LocalContext.current.getString(R.string.widget_retry_tracking_refresh_icon),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer)
            )
            reasonContent()
            Text(
                text = LocalContext.current.getString(R.string.widget_retry_tracking_text),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                ),
            )
        }
    }
}