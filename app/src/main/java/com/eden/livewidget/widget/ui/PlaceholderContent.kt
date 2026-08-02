package com.eden.livewidget.widget.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.eden.livewidget.R
import com.eden.livewidget.configurator.LivePointWidgetConfigurationActivity

@Composable
fun PlaceholderContent(context: Context?, id: GlanceId?) {

    val widgetId = if (context != null && id != null) GlanceAppWidgetManager(context).getAppWidgetId(id) else -1

    val configureIntent = if (context != null)
        Intent(
            context,
            LivePointWidgetConfigurationActivity::class.java
        )
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    else
        null

    Scaffold(
        backgroundColor = GlanceTheme.colors.widgetBackground,
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(
                onClick = if (configureIntent != null) actionStartActivity(intent = configureIntent) else object : Action {}
            ),

        ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(12.dp)
                .padding(all = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = LocalContext.current.getString(R.string.widget_configure_prompt_text),
                style = TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }

}