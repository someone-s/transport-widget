package com.eden.livewidget.widget.ui

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.eden.livewidget.R
import com.eden.livewidget.widget.RefreshLivePointWidgetCallback
import com.eden.livewidget.widget.update.UpdateCancelReceiver
import com.eden.livewidget.widget.update.UpdateScheduler
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ControlGroup(
    mode: MyContentMode,
    widgetId: Int
) {

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    FilledButton(
        icon =
            if (mode == MyContentMode.ACTIVE)
                ImageProvider(R.drawable.ic_widget_refresh)
            else
                ImageProvider(R.drawable.ic_shared_outlined_warning),
        text =
            LocalTime.now().format(timeFormatter),
        colors =
            if (mode == MyContentMode.ACTIVE)
                ButtonDefaults.buttonColors()
            else
                ButtonDefaults.buttonColors(
                    backgroundColor = GlanceTheme.colors.error,
                    contentColor = GlanceTheme.colors.onError
                ),
        onClick = actionRunCallback<RefreshLivePointWidgetCallback>(),
        maxLines = 1
    )

    if (mode == MyContentMode.ACTIVE) {

        Spacer(
            modifier = GlanceModifier.width(2.dp)
        )

        val explicitUpdateCancelIntent =
                getExplicitUpdateCancelIntent(LocalContext.current, widgetId)

        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .cornerRadius(20.dp)
                .background(GlanceTheme.colors.tertiary)
                .clickable(actionSendBroadcast(explicitUpdateCancelIntent)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_shared_outlined_pause),
                contentDescription = LocalContext.current.getString(R.string.widget_pause_button_icon),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onTertiary),
                modifier = GlanceModifier.size(16.dp)
            )
        }
    }
}

private fun getExplicitUpdateCancelIntent(context: Context, appWidgetId: Int): Intent {

    return Intent(context.applicationContext, UpdateCancelReceiver::class.java)
        .apply {
            putExtra(UpdateScheduler.APP_WIDGET_ID, appWidgetId)
        }
}