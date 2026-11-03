package com.eden.livewidget.logic.update

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.eden.livewidget.LiveWidgetApplication
import com.eden.livewidget.data.widget.structure.FetchState
import com.eden.livewidget.data.widget.structure.WidgetState
import com.eden.livewidget.ui.widget.LivePointWidget
import kotlin.time.Duration

class UpdateBeginCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appContainer = (context.applicationContext as LiveWidgetApplication).container
        val widgetRepository = appContainer.widgetRepository

        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        widgetRepository.setState(appWidgetId, WidgetState(FetchState.PENDING))

        UpdateScheduler.replaceCurrentRequest(context, appWidgetId, 3, Duration.ZERO)

        // Update glance widget immediately
        val updater = LivePointWidget()
        updater.update(context, glanceId)
    }
}