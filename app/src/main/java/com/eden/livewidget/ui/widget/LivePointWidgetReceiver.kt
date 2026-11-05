package com.eden.livewidget.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.eden.livewidget.logic.update.UpdateService

class LivePointWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LivePointWidget()

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            context.startService(
                Intent(context, UpdateService::class.java).apply {
                    action = UpdateService.ACTION_DELETE
                    putExtra(UpdateService.EXTRA_APP_WIDGET_ID, it)
                }
            )
        }
        super.onDeleted(context, appWidgetIds)
    }
}