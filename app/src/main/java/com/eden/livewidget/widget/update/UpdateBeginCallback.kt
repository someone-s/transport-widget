package com.eden.livewidget.widget.update

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.eden.livewidget.widget.LivePointWidget
import kotlin.time.Duration

class UpdateBeginCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        updateAppWidgetState(context, glanceId) { preferences ->
            preferences[LivePointWidget.FETCH_STATE_KEY] = LivePointWidget.FETCH_PENDING
        }

        UpdateScheduler.setCurrentRequest(context, widgetId, 3, Duration.ZERO)

        // Update glance widget immediately
        val updater = LivePointWidget()
        updater.update(context, glanceId)
    }
}