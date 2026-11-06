package com.eden.livewidget.ui.widget

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
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.components.FilledButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.eden.livewidget.R
import com.eden.livewidget.logic.update.UpdateService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ControlGroup(
    mode: MyContentMode,
    appWidgetId: Int,
    lastUpdate: LocalDateTime?,
) {
    val context = LocalContext.current

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeInvalid = "‒‒:‒‒"
    
    val isActive =
        when(mode) {
            MyContentMode.ACTIVE_VALID,
            MyContentMode.ACTIVE_UNINITIALIZED,
            MyContentMode.ACTIVE_RETRY -> true
            else -> false
        }
    Row {
        FilledButton(
            icon =
                if (isActive)
                    ImageProvider(R.drawable.ic_widget_refresh)
                else
                    ImageProvider(R.drawable.ic_shared_outlined_warning),
            text =
                lastUpdate?.format(timeFormatter) ?: timeInvalid,
            colors =
                if (isActive)
                    ButtonDefaults.buttonColors()
                else
                    ButtonDefaults.buttonColors(
                        backgroundColor = GlanceTheme.colors.error,
                        contentColor = GlanceTheme.colors.onError
                    ),
            onClick = actionStartService(
                isForegroundService = true,
                intent = Intent(context, UpdateService::class.java).apply {
                    action = UpdateService.ACTION_UPDATE
                    putExtra(UpdateService.EXTRA_APP_WIDGET_ID, appWidgetId)
                }
            ),
            maxLines = 1,
        )

        if (isActive) {

            Spacer(
                modifier = GlanceModifier.width(2.dp)
            )

            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(20.dp)
                    .background(GlanceTheme.colors.tertiary)
                    .clickable(actionStartService(
                        isForegroundService = false,
                        intent = Intent(context, UpdateService::class.java).apply {
                            action = UpdateService.ACTION_PAUSE
                            putExtra(UpdateService.EXTRA_APP_WIDGET_ID, appWidgetId)
                        }
                    )),
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
}